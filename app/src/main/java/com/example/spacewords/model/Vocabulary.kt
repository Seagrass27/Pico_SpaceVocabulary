package com.example.spacewords.model

import androidx.compose.ui.graphics.Color

data class Topic(
    val id: String,
    val title: String,
    val subtitle: String,
    val tint: Color,
    val words: List<VocabWord>
)

data class VocabWord(
    val id: String,
    val topicId: String,
    val word: String,
    val phonetic: String,
    val meaningZh: String,
    val example: String
)

val Topics = listOf(
    topic(
        id = "food",
        title = "FOOD",
        subtitle = "Meals, taste, and restaurant words",
        tint = Color(0xFFE9B85B),
        words = listOf(
            entry("appetizer", "/ˈæpɪtaɪzər/", "开胃菜", "We ordered a light appetizer before dinner."),
            entry("ingredient", "/ɪnˈɡriːdiənt/", "食材", "Fresh basil is the key ingredient in this sauce."),
            entry("flavor", "/ˈfleɪvər/", "味道", "The soup has a rich flavor."),
            entry("portion", "/ˈpɔːrʃn/", "一份", "The lunch portion is enough for two people."),
            entry("recipe", "/ˈresəpi/", "食谱", "I followed a simple recipe for pancakes."),
            entry("seasoning", "/ˈsiːzənɪŋ/", "调味料", "Add more seasoning if it tastes plain."),
            entry("dessert", "/dɪˈzɜːrt/", "甜点", "Fruit is my favorite dessert."),
            entry("takeout", "/ˈteɪkaʊt/", "外卖", "Let's get takeout after work.")
        )
    ),
    topic(
        id = "travel",
        title = "TRAVEL",
        subtitle = "Airport, hotel, and trip basics",
        tint = Color(0xFF6AB6E8),
        words = listOf(
            entry("itinerary", "/aɪˈtɪnəreri/", "行程", "Our itinerary includes two museums."),
            entry("boarding", "/ˈbɔːrdɪŋ/", "登机", "Boarding starts at gate twelve."),
            entry("luggage", "/ˈlʌɡɪdʒ/", "行李", "My luggage is still at the airport."),
            entry("reservation", "/ˌrezərˈveɪʃn/", "预订", "I made a hotel reservation online."),
            entry("destination", "/ˌdestɪˈneɪʃn/", "目的地", "Tokyo is our final destination."),
            entry("departure", "/dɪˈpɑːrtʃər/", "出发", "The departure time changed."),
            entry("souvenir", "/ˌsuːvəˈnɪr/", "纪念品", "She bought a small souvenir."),
            entry("customs", "/ˈkʌstəmz/", "海关", "We went through customs quickly.")
        )
    ),
    topic(
        id = "office",
        title = "OFFICE",
        subtitle = "Workplace routines and tools",
        tint = Color(0xFF8FA6EA),
        words = listOf(
            entry("agenda", "/əˈdʒendə/", "议程", "The agenda has three items."),
            entry("deadline", "/ˈdedlaɪn/", "截止日期", "The deadline is Friday afternoon."),
            entry("briefing", "/ˈbriːfɪŋ/", "简报", "We have a short briefing at nine."),
            entry("proposal", "/prəˈpoʊzl/", "提案", "The team reviewed the proposal."),
            entry("colleague", "/ˈkɑːliːɡ/", "同事", "My colleague helped me prepare the report."),
            entry("schedule", "/ˈskedʒuːl/", "日程", "Please check my schedule first."),
            entry("feedback", "/ˈfiːdbæk/", "反馈", "Thanks for your useful feedback."),
            entry("spreadsheet", "/ˈspredʃiːt/", "电子表格", "Update the spreadsheet before lunch.")
        )
    ),
    topic(
        id = "interview",
        title = "INTERVIEW",
        subtitle = "Hiring conversations and self-introduction",
        tint = Color(0xFFB698F0),
        words = listOf(
            entry("candidate", "/ˈkændɪdeɪt/", "候选人", "The candidate answered confidently."),
            entry("strength", "/streŋθ/", "优势", "Communication is one of my strengths."),
            entry("experience", "/ɪkˈspɪriəns/", "经验", "I have three years of experience."),
            entry("qualification", "/ˌkwɑːlɪfɪˈkeɪʃn/", "资质", "This role requires a teaching qualification."),
            entry("portfolio", "/pɔːrtˈfoʊlioʊ/", "作品集", "I shared my design portfolio."),
            entry("availability", "/əˌveɪləˈbɪləti/", "可入职时间", "What is your availability next week?"),
            entry("reference", "/ˈrefrəns/", "推荐人", "Please provide one reference."),
            entry("negotiate", "/nɪˈɡoʊʃieɪt/", "协商", "We can negotiate the start date.")
        )
    ),
    topic(
        id = "workout",
        title = "WORKOUT",
        subtitle = "Fitness actions and training plans",
        tint = Color(0xFF73C7A4),
        words = listOf(
            entry("stretch", "/stretʃ/", "拉伸", "Stretch before you start running."),
            entry("endurance", "/ɪnˈdʊrəns/", "耐力", "Cycling improves endurance."),
            entry("routine", "/ruːˈtiːn/", "训练计划", "My morning routine includes yoga."),
            entry("posture", "/ˈpɑːstʃər/", "姿势", "Good posture prevents injury."),
            entry("repetition", "/ˌrepəˈtɪʃn/", "重复次数", "Do ten repetitions for each set."),
            entry("hydrate", "/ˈhaɪdreɪt/", "补水", "Remember to hydrate during exercise."),
            entry("balance", "/ˈbæləns/", "平衡", "This exercise trains your balance."),
            entry("recovery", "/rɪˈkʌvəri/", "恢复", "Sleep is important for recovery.")
        )
    ),
    topic(
        id = "education",
        title = "EDUCATION",
        subtitle = "Study, class, and learning goals",
        tint = Color(0xFFD5B35D),
        words = listOf(
            entry("assignment", "/əˈsaɪnmənt/", "作业", "The assignment is due tomorrow."),
            entry("lecture", "/ˈlektʃər/", "讲座", "The lecture starts at ten."),
            entry("curriculum", "/kəˈrɪkjələm/", "课程体系", "The curriculum includes speaking practice."),
            entry("semester", "/sɪˈmestər/", "学期", "This semester is very busy."),
            entry("scholarship", "/ˈskɑːlərʃɪp/", "奖学金", "She applied for a scholarship."),
            entry("tuition", "/tuˈɪʃn/", "学费", "Tuition is paid each term."),
            entry("graduate", "/ˈɡrædʒueɪt/", "毕业", "He will graduate next year."),
            entry("review", "/rɪˈvjuː/", "复习", "Let's review yesterday's lesson.")
        )
    )
)

val AllWords = Topics.flatMap { it.words }

fun findTopic(id: String?): Topic = Topics.firstOrNull { it.id == id } ?: Topics.first()

fun findWord(id: String): VocabWord? = AllWords.firstOrNull { it.id == id }

private fun topic(
    id: String,
    title: String,
    subtitle: String,
    tint: Color,
    words: List<WordSeed>
): Topic = Topic(
    id = id,
    title = title,
    subtitle = subtitle,
    tint = tint,
    words = words.map { seed ->
        VocabWord(
            id = "$id-${seed.word}",
            topicId = id,
            word = seed.word,
            phonetic = seed.phonetic,
            meaningZh = seed.meaningZh,
            example = seed.example
        )
    }
)

private data class WordSeed(
    val word: String,
    val phonetic: String,
    val meaningZh: String,
    val example: String
)

private fun entry(word: String, phonetic: String, meaningZh: String, example: String) =
    WordSeed(word, phonetic, meaningZh, example)
