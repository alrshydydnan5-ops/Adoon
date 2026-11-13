package com.example.data.catalog

import com.example.data.model.FoodCatalogItem
import com.example.data.model.NutritionTip
import com.example.data.model.SubscriptionPlan
import com.example.data.model.WorkoutCatalogItem

object HealthCatalog {

    val foods: List<FoodCatalogItem> = listOf(
        FoodCatalogItem(
            id = "eggs_boiled",
            nameAr = "بيض مسلوق (3 حبات)",
            nameEn = "Boiled Eggs (3 large)",
            mealType = "breakfast",
            calories = 234,
            carbsG = 1.8,
            proteinG = 18.9,
            fatsG = 15.9,
            servingAr = "3 حبات",
            servingEn = "3 large eggs",
            categoryTag = "protein"
        ),
        FoodCatalogItem(
            id = "oats_milk_honey",
            nameAr = "شوفان بالحليب والعسل",
            nameEn = "Oatmeal with Milk & Honey",
            mealType = "breakfast",
            calories = 340,
            carbsG = 54.0,
            proteinG = 13.0,
            fatsG = 7.5,
            servingAr = "كوب مطبوخ",
            servingEn = "1 cooked bowl",
            categoryTag = "carbs"
        ),
        FoodCatalogItem(
            id = "foul_medames",
            nameAr = "فول مدمس بزيت الزيتون",
            nameEn = "Foul Medames & Olive Oil",
            mealType = "breakfast",
            calories = 290,
            carbsG = 38.0,
            proteinG = 14.5,
            fatsG = 9.0,
            servingAr = "صحن متوسط",
            servingEn = "1 medium bowl",
            categoryTag = "balanced"
        ),
        FoodCatalogItem(
            id = "greek_yogurt_berries",
            nameAr = "زبادي يوناني مع التوت",
            nameEn = "Greek Yogurt & Berries",
            mealType = "breakfast",
            calories = 185,
            carbsG = 16.0,
            proteinG = 18.0,
            fatsG = 4.5,
            servingAr = "170 جم",
            servingEn = "170g cup",
            categoryTag = "protein"
        ),
        FoodCatalogItem(
            id = "labneh_olive_oil",
            nameAr = "لبنة قليلة الدسم مع زيت زيتون",
            nameEn = "Light Labneh & Olive Oil",
            mealType = "breakfast",
            calories = 210,
            carbsG = 6.0,
            proteinG = 11.0,
            fatsG = 15.0,
            servingAr = "100 جم",
            servingEn = "100g serving",
            categoryTag = "fats"
        ),
        FoodCatalogItem(
            id = "grilled_chicken_breast",
            nameAr = "صدر دجاج مشوي بالأعشاب",
            nameEn = "Grilled Herb Chicken Breast",
            mealType = "lunch",
            calories = 330,
            carbsG = 2.0,
            proteinG = 58.0,
            fatsG = 7.5,
            servingAr = "200 جم",
            servingEn = "200g fillet",
            categoryTag = "protein"
        ),
        FoodCatalogItem(
            id = "healthy_chicken_kabsa",
            nameAr = "كبسة دجاج صحية (أرز بسمتي)",
            nameEn = "Healthy Chicken Kabsa Bowl",
            mealType = "lunch",
            calories = 520,
            carbsG = 58.0,
            proteinG = 42.0,
            fatsG = 12.0,
            servingAr = "وجبة متكاملة",
            servingEn = "1 full plate",
            categoryTag = "balanced"
        ),
        FoodCatalogItem(
            id = "shawarma_protein_bowl",
            nameAr = "صحن شاورما دجاج دايت",
            nameEn = "High-Protein Chicken Shawarma Bowl",
            mealType = "lunch",
            calories = 460,
            carbsG = 34.0,
            proteinG = 46.0,
            fatsG = 14.0,
            servingAr = "350 جم",
            servingEn = "350g bowl",
            categoryTag = "protein"
        ),
        FoodCatalogItem(
            id = "grilled_salmon",
            nameAr = "سلمون مشوي بالليمون",
            nameEn = "Grilled Lemon Salmon",
            mealType = "dinner",
            calories = 395,
            carbsG = 1.5,
            proteinG = 39.0,
            fatsG = 24.0,
            servingAr = "180 جم",
            servingEn = "180g fillet",
            categoryTag = "fats"
        ),
        FoodCatalogItem(
            id = "basmati_rice",
            nameAr = "أرز بسمتي مطبوخ",
            nameEn = "Steamed Basmati Rice",
            mealType = "lunch",
            calories = 210,
            carbsG = 45.0,
            proteinG = 4.5,
            fatsG = 0.8,
            servingAr = "150 جم",
            servingEn = "150g cooked",
            categoryTag = "carbs"
        ),
        FoodCatalogItem(
            id = "sweet_potato",
            nameAr = "بطاطا حلوة مشوية",
            nameEn = "Roasted Sweet Potato",
            mealType = "lunch",
            calories = 180,
            carbsG = 41.0,
            proteinG = 4.0,
            fatsG = 0.3,
            servingAr = "200 جم",
            servingEn = "200g medium",
            categoryTag = "carbs"
        ),
        FoodCatalogItem(
            id = "tuna_salad",
            nameAr = "سلطة تونة بالماء والذرة",
            nameEn = "Water-Packed Tuna Salad",
            mealType = "dinner",
            calories = 240,
            carbsG = 10.0,
            proteinG = 38.0,
            fatsG = 4.0,
            servingAr = "علبة + خضار",
            servingEn = "1 can + greens",
            categoryTag = "protein"
        ),
        FoodCatalogItem(
            id = "lentil_soup",
            nameAr = "شوربة عدس دافئة",
            nameEn = "Warm Red Lentil Soup",
            mealType = "dinner",
            calories = 220,
            carbsG = 34.0,
            proteinG = 13.0,
            fatsG = 3.5,
            servingAr = "صحن 300 مل",
            servingEn = "300ml bowl",
            categoryTag = "carbs"
        ),
        FoodCatalogItem(
            id = "quinoa_tabbouleh",
            nameAr = "تبولة بالكينوا والبقدونس",
            nameEn = "Quinoa Tabbouleh Salad",
            mealType = "dinner",
            calories = 250,
            carbsG = 28.0,
            proteinG = 8.5,
            fatsG = 11.5,
            servingAr = "صحن 220 جم",
            servingEn = "220g bowl",
            categoryTag = "balanced"
        ),
        FoodCatalogItem(
            id = "whey_protein_shake",
            nameAr = "مخفوق واي بروتين",
            nameEn = "Whey Protein Isolate Shake",
            mealType = "snack",
            calories = 130,
            carbsG = 3.0,
            proteinG = 27.0,
            fatsG = 1.2,
            servingAr = "سكوب + ماء",
            servingEn = "1 scoop + water",
            categoryTag = "protein"
        ),
        FoodCatalogItem(
            id = "dates_almonds",
            nameAr = "3 تمرات مع لوز نيء",
            nameEn = "3 Medjool Dates & Raw Almonds",
            mealType = "snack",
            calories = 195,
            carbsG = 28.0,
            proteinG = 4.5,
            fatsG = 8.0,
            servingAr = "3 تمرات + 15 جم لوز",
            servingEn = "3 dates + 15g almonds",
            categoryTag = "carbs"
        ),
        FoodCatalogItem(
            id = "avocado_toast",
            nameAr = "توست أسمر بالأفوكادو",
            nameEn = "Whole-Grain Avocado Toast",
            mealType = "breakfast",
            calories = 265,
            carbsG = 24.0,
            proteinG = 7.0,
            fatsG = 15.5,
            servingAr = "شريحة + نصف أفوكادو",
            servingEn = "1 slice + 1/2 avocado",
            categoryTag = "fats"
        ),
        FoodCatalogItem(
            id = "berry_banana_smoothie",
            nameAr = "سموثي موز وتوت وحليب",
            nameEn = "Banana Berry Recovery Smoothie",
            mealType = "snack",
            calories = 240,
            carbsG = 42.0,
            proteinG = 12.0,
            fatsG = 3.0,
            servingAr = "كوب 350 مل",
            servingEn = "350ml glass",
            categoryTag = "carbs"
        )
    )

    val workouts: List<WorkoutCatalogItem> = listOf(
        WorkoutCatalogItem(
            id = "strength_upper",
            titleAr = "تمارين مقاومة الجزء العلوي",
            titleEn = "Upper Body Strength Training",
            category = "strength",
            metValue = 5.5,
            defaultMinutes = 45,
            intensityAr = "متوسط إلى عالي",
            intensityEn = "Moderate-High"
        ),
        WorkoutCatalogItem(
            id = "strength_lower",
            titleAr = "تمارين الأرجل والسكوات",
            titleEn = "Lower Body & Squats",
            category = "strength",
            metValue = 6.0,
            defaultMinutes = 45,
            intensityAr = "عالي",
            intensityEn = "High"
        ),
        WorkoutCatalogItem(
            id = "hiit_burn",
            titleAr = "حرق دهون HIIT متقطع",
            titleEn = "HIIT Fat-Burn Intervals",
            category = "hiit",
            metValue = 9.0,
            defaultMinutes = 25,
            intensityAr = "مكثف",
            intensityEn = "Intense"
        ),
        WorkoutCatalogItem(
            id = "brisk_walk",
            titleAr = "مشي سريع في الهواء الطلق",
            titleEn = "Brisk Outdoor Walking",
            category = "cardio",
            metValue = 4.3,
            defaultMinutes = 40,
            intensityAr = "معتدل",
            intensityEn = "Moderate"
        ),
        WorkoutCatalogItem(
            id = "running_5k",
            titleAr = "جري متوسط السرعة",
            titleEn = "Steady-Pace Running",
            category = "cardio",
            metValue = 9.8,
            defaultMinutes = 30,
            intensityAr = "عالي",
            intensityEn = "High"
        ),
        WorkoutCatalogItem(
            id = "cycling",
            titleAr = "ركوب الدراجة الثابتة أو الطريق",
            titleEn = "Indoor / Road Cycling",
            category = "cardio",
            metValue = 7.5,
            defaultMinutes = 35,
            intensityAr = "متوسط-عالي",
            intensityEn = "Vigorous"
        ),
        WorkoutCatalogItem(
            id = "swimming",
            titleAr = "سباحة حرة",
            titleEn = "Lap Swimming",
            category = "sports",
            metValue = 8.0,
            defaultMinutes = 35,
            intensityAr = "عالي",
            intensityEn = "High"
        ),
        WorkoutCatalogItem(
            id = "football_match",
            titleAr = "مباراة كرة قدم",
            titleEn = "Football / Soccer Match",
            category = "sports",
            metValue = 8.5,
            defaultMinutes = 60,
            intensityAr = "مكثف",
            intensityEn = "Intense"
        ),
        WorkoutCatalogItem(
            id = "home_bodyweight",
            titleAr = "تمارين وزن الجسم بالمنزل",
            titleEn = "Home Bodyweight Circuit",
            category = "strength",
            metValue = 5.0,
            defaultMinutes = 30,
            intensityAr = "متوسط",
            intensityEn = "Moderate"
        ),
        WorkoutCatalogItem(
            id = "yoga_mobility",
            titleAr = "يوجا وإطالة واستشفاء",
            titleEn = "Yoga & Mobility Recovery",
            category = "flexibility",
            metValue = 3.0,
            defaultMinutes = 30,
            intensityAr = "خفيف",
            intensityEn = "Light"
        )
    )

    val tips: List<NutritionTip> = listOf(
        NutritionTip(
            id = "tip_protein_pacing",
            category = "protein",
            titleAr = "وزّع البروتين على 4 وجبات",
            titleEn = "Space Protein Across 4 Meals",
            bodyAr = "تناول 25 إلى 40 جرام بروتين كل 3-4 ساعات يحفز بناء العضلات بشكل أفضل من تجميعه في وجبة واحدة، ويمنحك شبعا طويلا.",
            bodyEn = "Consuming 25–40g of protein every 3–4 hours maximizes muscle protein synthesis and keeps hunger hormones steady all day.",
            badgeAr = "بناء وشبع",
            badgeEn = "Muscle & Satiety"
        ),
        NutritionTip(
            id = "tip_smart_carbs",
            category = "carbs",
            titleAr = "اختر الكارب المعقد حول وقت التمرين",
            titleEn = "Time Complex Carbs Around Training",
            bodyAr = "تناول الشوفان، البطاطا الحلوة، أو الأرز البسمتي قبل التمرين بساعة إلى ساعتين يمنحك طاقة ثابتة دون ارتفاع مفاجئ في الأنسولين.",
            bodyEn = "Eating oats, sweet potato, or basmati rice 1–2 hours before workouts fuels peak performance without blood sugar crashes.",
            badgeAr = "طاقة ذكية",
            badgeEn = "Smart Fuel"
        ),
        NutritionTip(
            id = "tip_hydration",
            category = "hydration",
            titleAr = "كوب ماء قبل كل وجبة",
            titleEn = "Hydrate Before Every Meal",
            bodyAr = "شرب 300-400 مل من الماء قبل الوجبات بـ 15 دقيقة يحسن الهضم، يقلل الخلط بين العطش والجوع، ويرفع التركيز.",
            bodyEn = "Drinking 300–400ml of water 15 minutes before meals supports digestion, prevents thirst-driven snacking, and boosts alertness.",
            badgeAr = "ترطيب",
            badgeEn = "Hydration"
        ),
        NutritionTip(
            id = "tip_fat_loss",
            category = "weight_loss",
            titleAr = "العجز المعتدل يحفظ العضلات",
            titleEn = "Moderate Deficit Protects Muscle",
            bodyAr = "عجز 350 إلى 500 سعرة يوميا مع بروتين كافٍ يضمن نزول الدهون تدريجيا دون فقدان الكتلة العضلية أو هبوط النشاط.",
            bodyEn = "A 350–500 kcal daily deficit paired with adequate protein drives steady fat loss while preserving lean muscle mass.",
            badgeAr = "تنشيف صحي",
            badgeEn = "Lean Cut"
        ),
        NutritionTip(
            id = "tip_healthy_fats",
            category = "fats",
            titleAr = "لا تهمل الدهون الصحية للهرمونات",
            titleEn = "Keep Healthy Fats for Hormones",
            bodyAr = "زيت الزيتون، الأفوكادو، السلمون، والمكسرات النيئة تدعم صحة المفاصل وتوازن الهرمونات وامتصاص الفيتامينات.",
            bodyEn = "Olive oil, avocado, salmon, and raw nuts support joint recovery, hormone balance, and fat-soluble vitamin absorption.",
            badgeAr = "توازن هرموني",
            badgeEn = "Hormone Health"
        ),
        NutritionTip(
            id = "tip_recovery_sleep",
            category = "workout",
            titleAr = "النوم العميق هو نصف النتيجة",
            titleEn = "Deep Sleep Completes Recovery",
            bodyAr = "الحصول على 7-8 ساعات نوم يقلل هرمون الجوع (الجريلين) بنسبة كبيرة ويساعد جسمك على حرق الدهون واستشفاء الألياف العضلية.",
            bodyEn = "Getting 7–8 hours of sleep lowers hunger hormones (ghrelin), sharpens insulin sensitivity, and repairs trained muscle fibers.",
            badgeAr = "استشفاء",
            badgeEn = "Recovery"
        ),
        NutritionTip(
            id = "tip_fiber_first",
            category = "carbs",
            titleAr = "ابدأ وجبتك بالسلطة والألياف",
            titleEn = "Start Meals With Fiber & Greens",
            bodyAr = "تناول الخضروات الورقية قبل الكربوهيدرات يبطئ امتصاص السكر في الدم بنسبة تصل إلى 30% ويحافظ على طاقتك بعد الأكل.",
            bodyEn = "Eating leafy greens before starches blunts post-meal glucose spikes by up to 30% and prevents afternoon fatigue.",
            badgeAr = "توازن السكر",
            badgeEn = "Glucose Control"
        ),
        NutritionTip(
            id = "tip_post_workout",
            category = "workout",
            titleAr = "وجبة ما بعد التمرين خلال ساعتين",
            titleEn = "Refuel Within 2 Hours Post-Workout",
            bodyAr = "ادمج بين بروتين سريع الامتصاص وكارب متوسط بعد تمرين المقاومة لإعادة تعبئة الجليكوجين وتسريع التعافي.",
            bodyEn = "Pair lean protein with clean carbs after resistance training to replenish muscle glycogen and speed up recovery.",
            badgeAr = "بعد التمرين",
            badgeEn = "Post-Workout"
        )
    )

    val plans: List<SubscriptionPlan> = listOf(
        SubscriptionPlan(
            tier = "free",
            titleAr = "الباقة المجانية",
            titleEn = "Free Starter",
            priceSar = 0,
            periodAr = "مجاناً للأبد",
            periodEn = "Free forever",
            savingsAr = null,
            savingsEn = null,
            featuresAr = listOf(
                "حاسبة السعرات والكارب والبروتين",
                "تسجيل الوجبات والماء والتمارين",
                "ملخص التقدم اليومي"
            ),
            featuresEn = listOf(
                "Calorie, Carbs & Protein Calculator",
                "Daily Meals, Water & Workout Logging",
                "Daily Progress Summary"
            )
        ),
        SubscriptionPlan(
            tier = "monthly",
            titleAr = "برو الشهري",
            titleEn = "Pro Monthly",
            priceSar = 29,
            periodAr = "29 ر.س / شهرياً",
            periodEn = "29 SAR / month",
            savingsAr = "الأكثر مرونة",
            savingsEn = "Flexible",
            featuresAr = listOf(
                "تحليلات تقدم أسبوعية متقدمة",
                "تنبيهات ذكية مخصصة غير محدودة",
                "خطط ماكرو متنوعة (كيتو / تضخيم / تنشيف)",
                "مزامنة سحابية فورية"
            ),
            featuresEn = listOf(
                "Advanced 7-Day & 14-Day Analytics",
                "Unlimited Custom Smart Reminders",
                "Custom Macro Splits (Keto / Bulk / Cut)",
                "Instant Cloud Sync"
            )
        ),
        SubscriptionPlan(
            tier = "quarterly",
            titleAr = "برو 3 أشهر",
            titleEn = "Pro Quarterly",
            priceSar = 69,
            periodAr = "69 ر.س / 3 أشهر",
            periodEn = "69 SAR / 3 months",
            savingsAr = "وفّر 20%",
            savingsEn = "Save 20%",
            featuresAr = listOf(
                "جميع مزايا برو الشهري",
                "نصائح تغذية مخصصة حسب ماكرو يومك",
                "أولوية الحفظ السحابي"
            ),
            featuresEn = listOf(
                "All Pro Monthly features",
                "Dynamic Macro-Aware Nutrition Tips",
                "Priority Cloud Backup"
            )
        ),
        SubscriptionPlan(
            tier = "annual",
            titleAr = "الأبطال السنوي",
            titleEn = "Annual Champion",
            priceSar = 199,
            periodAr = "199 ر.س / سنوياً",
            periodEn = "199 SAR / year",
            savingsAr = "أفضل قيمة - وفّر 43%",
            savingsEn = "Best Value - Save 43%",
            featuresAr = listOf(
                "جميع الميزات الاحترافية مفتوحة بالكامل",
                "تفعيل فوري برقم العضوية الرقمي البسيط",
                "دعم كل طرق الدفع والتقسيط",
                "تحليلات وزن وسعرات شاملة"
            ),
            featuresEn = listOf(
                "All Pro Features Unlocked",
                "Instant Numeric Member ID Activation",
                "All Payment & Split-Pay Options",
                "Full Weight & Macro Trend Analytics"
            )
        )
    )
}
