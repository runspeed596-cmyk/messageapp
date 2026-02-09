package com.Kelasor.app.data

data class Coordinates(
    val lat: Double,
    val lon: Double
)

data class University(
    val id: String,
    val name: String,
    val coordinates: Coordinates,
    val province: String = "",
    val city: String = "",
    val ministry: String = "وزارت علوم، تحقیقات و فناوری",
    val type: String = "دولتی",
    val establishmentYear: String = "۱۳۱۳",
    val studentCount: Int = 30000,
    val faculties: List<String> = listOf("مهندسی", "علوم انسانی", "پزشکی"),
    val majors: List<String> = listOf("نرم‌افزار", "عمران", "حقوق"),
    val iranRank: Int = 1,
    val worldRank: Int = 400,
    val paperCount: Int = 12000,
    val journalCount: Int = 45,
    val facilities: List<String> = listOf("کتابخانه مرکزی", "خوابگاه", "سالن ورزشی")
)

data class City(
    val id: String,
    val name: String,
    val coordinates: Coordinates,
    val universities: List<University>
)

data class Province(
    val id: String,
    val name: String,
    val cities: List<City>
)

object ProvincesData {
    val iranCenter = Coordinates(32.4279, 53.6880)

    val provinces = listOf(
        Province(
            id = "tehran_prov",
            name = "تهران",
            cities = listOf(
                City(
                    id = "tehran_city",
                    name = "تهران",
                    coordinates = Coordinates(35.6892, 51.3890),
                    universities = listOf(
                        University(
                            id = "ut", 
                            name = "دانشگاه تهران", 
                            coordinates = Coordinates(35.7117, 51.3918),
                            iranRank = 1,
                            worldRank = 350,
                            studentCount = 45000,
                            faculties = listOf("ادبیات", "حقوق", "فنی", "کشاورزی"),
                            majors = listOf("مهندسی کامپیوتر", "معماری", "فلسفه")
                        ),
                        University(
                            id = "sharif", 
                            name = "دانشگاه صنعتی شریف", 
                            coordinates = Coordinates(35.7036, 51.3515),
                            iranRank = 2,
                            worldRank = 405,
                            studentCount = 12000,
                            type = "دولتی صنعتی",
                            faculties = listOf("مهندسی برق", "مهندسی مکانیک", "مهندسی کامپیوتر"),
                            majors = listOf("هوش مصنوعی", "امنیت شبکه", "رباتیک")
                        ),
                        University(
                            id = "aut", 
                            name = "دانشگاه صنعتی امیرکبیر", 
                            coordinates = Coordinates(35.7011, 51.4080),
                            iranRank = 4,
                            worldRank = 500,
                            studentCount = 14000
                        )
                    )
                )
            )
        ),
        Province(
            id = "fars_prov",
            name = "فارس",
            cities = listOf(
                City(
                    id = "shiraz_city",
                    name = "شیراز",
                    coordinates = Coordinates(29.5918, 52.5837),
                    universities = listOf(
                        University(
                            id = "shirazu", 
                            name = "دانشگاه شیراز", 
                            coordinates = Coordinates(29.6465, 52.5255),
                            iranRank = 3,
                            worldRank = 480,
                            studentCount = 18000
                        )
                    )
                )
            )
        ),
        Province(
            id = "isfahan_prov",
            name = "اصفهان",
            cities = listOf(
                City(
                    id = "isfahan_city",
                    name = "اصفهان",
                    coordinates = Coordinates(32.6539, 51.6660),
                    universities = listOf(
                        University(
                            id = "iut", 
                            name = "دانشگاه صنعتی اصفهان", 
                            coordinates = Coordinates(32.7208, 51.5273),
                            iranRank = 5,
                            worldRank = 550
                        ),
                        University(
                            id = "ui", 
                            name = "دانشگاه اصفهان", 
                            coordinates = Coordinates(32.6164, 51.6685),
                            iranRank = 7,
                            worldRank = 650
                        )
                    )
                )
            )
        ),
        Province(
            id = "khorasan_razavi_prov",
            name = "خراسان رضوی",
            cities = listOf(
                City(
                    id = "mashhad_city",
                    name = "مشهد",
                    coordinates = Coordinates(36.2605, 59.6168),
                    universities = listOf(
                        University(
                            id = "ferdowsi", 
                            name = "دانشگاه فردوسی مشهد", 
                            coordinates = Coordinates(36.3106, 59.5312),
                            iranRank = 3,
                            worldRank = 490
                        )
                    )
                )
            )
        ),
        Province(
            id = "azarbaijan_east_prov",
            name = "آذربایجان شرقی",
            cities = listOf(
                City(
                    id = "tabriz_city",
                    name = "تبریز",
                    coordinates = Coordinates(38.0962, 46.2919),
                    universities = listOf(
                        University(
                            id = "tabrizu", 
                            name = "دانشگاه تبریز", 
                            coordinates = Coordinates(38.0560, 46.3478),
                            iranRank = 6,
                            worldRank = 600
                        )
                    )
                )
            )
        )
    )

    fun getCityById(cityId: String): City? {
        return provinces.flatMap { it.cities }.find { it.id == cityId }
    }

    fun getAllUniversities(): List<University> {
        return provinces.flatMap { it.cities }.flatMap { it.universities }
    }
}
