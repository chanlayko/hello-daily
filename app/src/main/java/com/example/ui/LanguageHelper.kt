package com.example.ui

object LanguageHelper {
    private val translations = mapOf(
        "Settings" to mapOf(
            "English" to "Settings",
            "Myanmar" to "ဆက်တင်များ"
        ),
        "Preferences" to mapOf(
            "English" to "Preferences",
            "Myanmar" to "ဦးစားပေးမှုများ"
        ),
        "Manage Categories" to mapOf(
            "English" to "Manage Categories",
            "Myanmar" to "အမျိုးအစားများ စီမံရန်"
        ),
        "Configure transaction categories and custom icons" to mapOf(
            "English" to "Configure transaction categories and custom icons",
            "Myanmar" to "ငွေလွှဲအမျိုးအစားများနှင့် သင်္ကေတများ ပြင်ဆင်ရန်"
        ),
        "Visual Theme Mode" to mapOf(
            "English" to "Visual Theme Mode",
            "Myanmar" to "အပြင်အဆင်ပုံစံ (Theme)"
        ),
        "Configure application appearance theme" to mapOf(
            "English" to "Configure application appearance theme",
            "Myanmar" to "အက်ပ်၏ အရောင်နှင့် အပြင်အဆင်ပုံစံ ပြောင်းလဲရန်"
        ),
        "Active Currency" to mapOf(
            "English" to "Active Currency",
            "Myanmar" to "အသုံးပြုမည့် ငွေကြေးပုံစံ"
        ),
        "Current selected currency symbol: " to mapOf(
            "English" to "Current selected currency: ",
            "Myanmar" to "လက်ရှိအသုံးပြုနေသော ငွေကြေးသင်္ကေတ- "
        ),
        "Family Cloud Sync (Supabase)" to mapOf(
            "English" to "Family Cloud Sync (Supabase)",
            "Myanmar" to "မိသားစု Cloud Sync (Supabase)"
        ),
        "Export Records Report" to mapOf(
            "English" to "Export Records Report",
            "Myanmar" to "မှတ်တမ်းများကို ဖိုင်အဖြစ် ထုတ်ယူရန်"
        ),
        "Export Local Database (.json)" to mapOf(
            "English" to "Export Local Database (.json)",
            "Myanmar" to "ဒေတာများကို သိမ်းဆည်းရန် (.json)"
        ),
        "Import Database from JSON (.json)" to mapOf(
            "English" to "Import Database from JSON (.json)",
            "Myanmar" to "ဒေတာများကို ပြန်လည်သွင်းရန် (.json)"
        ),
        "Export PDF Report" to mapOf(
            "English" to "Export PDF Report",
            "Myanmar" to "PDF ဖြင့် အစီရင်ခံစာ ထုတ်ယူရန်"
        ),
        "Export CSV Records Spreadsheet" to mapOf(
            "English" to "Export CSV Records Spreadsheet",
            "Myanmar" to "CSV Spreadsheet ဖိုင်ထုတ်ယူရန်"
        ),
        "Dashboard" to mapOf(
            "English" to "Dashboard",
            "Myanmar" to "ပင်မစာမျက်နှာ"
        ),
        "Home" to mapOf(
            "English" to "Home",
            "Myanmar" to "ပင်မ"
        ),
        "History" to mapOf(
            "English" to "History",
            "Myanmar" to "မှတ်တမ်း"
        ),
        "To-Buy Notes" to mapOf(
            "English" to "To-Buy Notes",
            "Myanmar" to "ဝယ်စရာစာရင်း"
        ),
        "Notes" to mapOf(
            "English" to "Notes",
            "Myanmar" to "စာရင်း"
        ),
        "Analytics" to mapOf(
            "English" to "Analytics",
            "Myanmar" to "သုံးသပ်ချက်"
        ),
        "Budgets" to mapOf(
            "English" to "Budgets",
            "Myanmar" to "ဘတ်ဂျက်"
        ),
        "Language Select" to mapOf(
            "English" to "App Language",
            "Myanmar" to "အက်ပ် ဘာသာစကား"
        ),
        "Choose your preferred language" to mapOf(
            "English" to "Choose your preferred language",
            "Myanmar" to "အသုံးပြုလိုသည့် ဘာသာစကားကို ရွေးချယ်ပါ"
        ),
        "Welcome back," to mapOf(
            "English" to "Welcome back,",
            "Myanmar" to "မင်္ဂလာပါ၊"
        ),
        "Guest" to mapOf(
            "English" to "Guest User",
            "Myanmar" to "ဧည့်သည်"
        ),
        "Total Monthly Budget" to mapOf(
            "English" to "Total Monthly Budget",
            "Myanmar" to "လစဉ် စုစုပေါင်း ဘတ်ဂျက်"
        ),
        "Remaining Monthly Budget" to mapOf(
            "English" to "Remaining Monthly Budget",
            "Myanmar" to "ကျန်ရှိသော လစဉ် ဘတ်ဂျက်"
        ),
        "This Week" to mapOf(
            "English" to "This Week",
            "Myanmar" to "ယခုအပတ်"
        ),
        "Today" to mapOf(
            "English" to "Today",
            "Myanmar" to "ယနေ့"
        ),
        "To-Buy Items" to mapOf(
            "English" to "To-Buy Items",
            "Myanmar" to "ဝယ်ယူရန် ပစ္စည်းများ"
        ),
        "Monthly Expenses Trend" to mapOf(
            "English" to "Monthly Expenses Trend",
            "Myanmar" to "လစဉ် အသုံးစရိတ် လားရာပုံစံ"
        ),
        "Recent Expenses" to mapOf(
            "English" to "Recent Expenses",
            "Myanmar" to "လတ်တလော အသုံးစရိတ်များ"
        ),
        "View All" to mapOf(
            "English" to "View All",
            "Myanmar" to "အားလုံးကြည့်ရန်"
        ),
        "Add Transaction" to mapOf(
            "English" to "Add Transaction",
            "Myanmar" to "အသုံးစရိတ် အသစ်ထည့်ရန်"
        ),
        "Quick Add Expense" to mapOf(
            "English" to "Quick Add Expense",
            "Myanmar" to "အသုံးစရိတ် အမြန်ထည့်ရန်"
        ),
        "Title" to mapOf(
            "English" to "Title",
            "Myanmar" to "ခေါင်းစဉ်"
        ),
        "Amount" to mapOf(
            "English" to "Amount",
            "Myanmar" to "ပမာဏ"
        ),
        "Category" to mapOf(
            "English" to "Category",
            "Myanmar" to "အမျိုးအစား"
        ),
        "Date" to mapOf(
            "English" to "Date",
            "Myanmar" to "ရက်စွဲ"
        ),
        "Note" to mapOf(
            "English" to "Note",
            "Myanmar" to "မှတ်စု"
        ),
        "Cancel" to mapOf(
            "English" to "Cancel",
            "Myanmar" to "မလုပ်တော့ပါ"
        ),
        "Save" to mapOf(
            "English" to "Save",
            "Myanmar" to "သိမ်းဆည်းမည်"
        ),
        "Add Category" to mapOf(
            "English" to "Add Category",
            "Myanmar" to "အမျိုးအစား အသစ်ထည့်ရန်"
        ),
        "Name" to mapOf(
            "English" to "Name",
            "Myanmar" to "အမည်"
        ),
        "Icon" to mapOf(
            "English" to "Icon",
            "Myanmar" to "သင်္ကေတ"
        ),
        "Expense History" to mapOf(
            "English" to "Expense History",
            "Myanmar" to "အသုံးစရိတ် မှတ်တမ်းများ"
        ),
        "Search Expenses..." to mapOf(
            "English" to "Search Expenses...",
            "Myanmar" to "အသုံးစရိတ်များ ရှာဖွေရန်..."
        ),
        "Filter by Category" to mapOf(
            "English" to "Filter by Category",
            "Myanmar" to "အမျိုးအစားအလိုက် စစ်ထုတ်ရန်"
        ),
        "All Categories" to mapOf(
            "English" to "All Categories",
            "Myanmar" to "အမျိုးအစား အားလုံး"
        ),
        "Export Reports" to mapOf(
            "English" to "Export Reports",
            "Myanmar" to "အစီရင်ခံစာ ထုတ်ယူရန်"
        ),
        "Edit Expense" to mapOf(
            "English" to "Edit Expense",
            "Myanmar" to "အသုံးစရိတ် ပြင်ဆင်ရန်"
        ),
        "Delete Expense" to mapOf(
            "English" to "Delete Expense",
            "Myanmar" to "အသုံးစရိတ် ဖျက်ရန်"
        ),
        "Are you sure you want to delete this expense?" to mapOf(
            "English" to "Are you sure you want to delete this expense?",
            "Myanmar" to "ဤအသုံးစရိတ်ကို ဖျက်ရန် သေချာပါသလား။"
        ),
        "Manage Budgets" to mapOf(
            "English" to "Manage Budgets",
            "Myanmar" to "ဘတ်ဂျက် စီမံခန့်ခွဲမှု"
        ),
        "Daily Budget" to mapOf(
            "English" to "Daily Budget",
            "Myanmar" to "နေ့စဉ် ဘတ်ဂျက်"
        ),
        "Weekly Budget" to mapOf(
            "English" to "Weekly Budget",
            "Myanmar" to "အပတ်စဉ် ဘတ်ဂျက်"
        ),
        "Monthly Budget" to mapOf(
            "English" to "Monthly Budget",
            "Myanmar" to "လစဉ် ဘတ်ဂျက်"
        ),
        "Set" to mapOf(
            "English" to "Set",
            "Myanmar" to "သတ်မှတ်ရန်"
        ),
        "Configure Budget" to mapOf(
            "English" to "Configure Budget",
            "Myanmar" to "ဘတ်ဂျက် သတ်မှတ်ရန်"
        ),
        "Enter your limit" to mapOf(
            "English" to "Enter your limit",
            "Myanmar" to "ကန့်သတ်ပမာဏ ထည့်သွင်းပါ"
        ),
        "Limit Amount" to mapOf(
            "English" to "Limit Amount",
            "Myanmar" to "ကန့်သတ်ပမာဏ"
        ),
        "Shopping List" to mapOf(
            "English" to "Shopping List",
            "Myanmar" to "ဝယ်စရာစာရင်း"
        ),
        "Active Items" to mapOf(
            "English" to "Active Items",
            "Myanmar" to "ဝယ်ယူရန် ကျန်ရှိသည်များ"
        ),
        "Bought Items" to mapOf(
            "English" to "Bought Items",
            "Myanmar" to "ဝယ်ယူပြီးသည်များ"
        ),
        "Add Shopping Item" to mapOf(
            "English" to "Add Shopping Item",
            "Myanmar" to "ဝယ်ယူရန် ပစ္စည်းအသစ်ထည့်ရန်"
        ),
        "Estimated Amount" to mapOf(
            "English" to "Estimated Amount",
            "Myanmar" to "ခန့်မှန်းခြေ ပမာဏ"
        ),
        "Already Bought" to mapOf(
            "English" to "Already Bought",
            "Myanmar" to "ဝယ်ယူပြီး"
        ),
        "Delete Item" to mapOf(
            "English" to "Delete Item",
            "Myanmar" to "ပစ္စည်းဖျက်ရန်"
        ),
        "Are you sure you want to delete this shopping item?" to mapOf(
            "English" to "Are you sure you want to delete this shopping item?",
            "Myanmar" to "ဤဝယ်ယူရန် ပစ္စည်းကို ဖျက်ရန် သေချာပါသလား။"
        ),
        "Expense Analytics" to mapOf(
            "English" to "Expense Analytics",
            "Myanmar" to "အသုံးစရိတ် သုံးသပ်ချက်များ"
        ),
        "Today Summary" to mapOf(
            "English" to "Today Summary",
            "Myanmar" to "ယနေ့ အကျဉ်းချုပ်"
        ),
        "Week Summary" to mapOf(
            "English" to "Week Summary",
            "Myanmar" to "ယခုအပတ် အကျဉ်းချုပ်"
        ),
        "Month Summary" to mapOf(
            "English" to "Month Summary",
            "Myanmar" to "ယခုလ အကျဉ်းချုပ်"
        ),
        "Category Distribution" to mapOf(
            "English" to "Category Distribution",
            "Myanmar" to "အမျိုးအစားအလိုက် ခွဲခြမ်းစိတ်ဖြာချက်"
        ),
        "Weekly Spending Trend" to mapOf(
            "English" to "Weekly Spending Trend",
            "Myanmar" to "အပတ်စဉ် အသုံးစရိတ် လားရာပုံစံ"
        )
    )

    fun translate(key: String, language: String): String {
        return translations[key]?.get(language) ?: translations[key]?.get("English") ?: key
    }
}
