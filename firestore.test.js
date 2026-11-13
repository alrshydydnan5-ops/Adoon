const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const DATE_KEY = "2026-11-13";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfile(uid) {
  return {
    userId: uid,
    displayName: "Adnan",
    age: 28,
    gender: "male",
    heightCm: 178.0,
    weightKg: 76.5,
    startWeightKg: 80.0,
    targetWeightKg: 72.0,
    activityLevel: "moderate",
    goalType: "lose",
    targetCalories: 2200,
    targetCarbsG: 220,
    targetProteinG: 165,
    targetFatsG: 73,
    targetWaterMl: 2800,
    preferredLanguage: "ar",
    themeMode: "dark",
    subscriptionTier: "free",
    subscriptionNumber: "",
    subscriptionActive: false,
    createdAt: new Date(Date.now() - 2000),
    updatedAt: new Date(Date.now() - 2000),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("mealLogs").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("workoutLogs").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("dailyStats").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("reminders").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("paymentMethods").get()
  );
});

test("UserProfile: owner can create and update valid profile; rejects ghost fields & cross-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const aliceProfile = validProfile(ALICE_UID);
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(aliceProfile));

  // Cross-user read & write rejected
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacked" })
  );

  // Ghost field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Valid partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      subscriptionTier: "annual",
      subscriptionNumber: "0551234567",
      subscriptionActive: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("MealLogs & WorkoutLogs: owner CRUD succeeds; cross-user & invalid macros fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("mealLogs")
      .doc("meal_1")
      .set({
        userId: ALICE_UID,
        dateKey: DATE_KEY,
        nameAr: "صدر دجاج مشوي",
        nameEn: "Grilled Chicken Breast",
        mealType: "lunch",
        calories: 320,
        carbsG: 4.0,
        proteinG: 54.0,
        fatsG: 8.0,
        servingLabel: "200g",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("mealLogs").get()
  );

  // Invalid calories fail
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("mealLogs")
      .doc("meal_bad")
      .set({
        userId: ALICE_UID,
        dateKey: DATE_KEY,
        nameAr: "وجبة",
        nameEn: "Meal",
        mealType: "lunch",
        calories: 99999,
        carbsG: 10,
        proteinG: 10,
        fatsG: 10,
        servingLabel: "1x",
        createdAt: now,
        updatedAt: now,
      })
  );

  // Bob cannot read Alice's mealLogs
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("mealLogs").get()
  );

  // Workout log create & complete toggle
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("workoutLogs")
      .doc("work_1")
      .set({
        userId: ALICE_UID,
        dateKey: DATE_KEY,
        titleAr: "تمرين مقاومة",
        titleEn: "Strength Training",
        category: "strength",
        durationMinutes: 45,
        caloriesBurned: 310,
        completed: true,
        createdAt: now,
        updatedAt: now,
      })
  );
});

test("DailyStats, Reminders, PaymentMethods: owner CRUD succeeds and invalid inputs fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dailyStats")
      .doc(DATE_KEY)
      .set({
        userId: ALICE_UID,
        dateKey: DATE_KEY,
        waterMl: 1500,
        steps: 6500,
        weightKg: 76.2,
        createdAt: now,
        updatedAt: now,
      })
  );

  // Bad dateKey rejected
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dailyStats")
      .doc("bad-key")
      .set({
        userId: ALICE_UID,
        dateKey: "bad-key",
        waterMl: 500,
        steps: 0,
        weightKg: 0,
        createdAt: now,
        updatedAt: now,
      })
  );

  // Reminder
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("reminders")
      .doc("rem_1")
      .set({
        userId: ALICE_UID,
        titleAr: "شرب الماء",
        titleEn: "Drink Water",
        category: "water",
        hour: 10,
        minute: 30,
        enabled: true,
        createdAt: now,
        updatedAt: now,
      })
  );

  // Payment method
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("paymentMethods")
      .doc("pm_1")
      .set({
        userId: ALICE_UID,
        provider: "mada",
        label: "Mada Bank Card",
        maskedIdentifier: "•••• 4821",
        isDefault: true,
        createdAt: now,
        updatedAt: now,
      })
  );

  // Cross-user paymentMethods blocked
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("paymentMethods").get()
  );
});
