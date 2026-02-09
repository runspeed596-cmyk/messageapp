package com.iliyadev.springboot.config

import com.iliyadev.springboot.models.ElmEvent
import com.iliyadev.springboot.models.ElmEventType
import com.iliyadev.springboot.repositories.ElmEventRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ElmPeakDataInitializer {

    @Bean
    fun seedElmEvents(repository: ElmEventRepository): CommandLineRunner {
        return CommandLineRunner {
            if (repository.count() == 0L) {
                val events = listOf(
                    ElmEvent(
                        title = "مسابقات ملی هوش مصنوعی (شریف)",
                        description = "بزرگترین چالش پردازش تصویر و داده‌های کلان در سطح کشور ویژه دانشجویان فنی.",
                        date = "۱۵ اسفند ۱۴۰۲",
                        location = "دانشگاه صنعتی شریف",
                        organizer = "انجمن علمی کامپیوتر",
                        reward = "۵۰ میلیون تومان جایزه نقدی",
                        type = ElmEventType.COMPETITION
                    ),
                    ElmEvent(
                        title = "استارتاپ ویکند تخصصی انرژی",
                        description = "جذب سرمایه برای طرح‌های نوآورانه در حوزه انرژی‌های تجدیدپذیر و بهینه‌سازی.",
                        date = "۲۰ فروردین ۱۴۰۳",
                        location = "دانشگاه تهران - پردیس فنی",
                        organizer = "شتاب‌دهنده انرژیک",
                        reward = "حمایت مالی تا سقف ۵۰۰ میلیون",
                        type = ElmEventType.STARTUP
                    ),
                    ElmEvent(
                        title = "کنگره بین‌المللی نانوتکنولوژی",
                        description = "ارائه جدیدترین یافته‌های دانشمندان برتر جهان در حوزه نانوپزشکی و الکترونیک.",
                        date = "۲۵ اردیبهشت ۱۴۰۳",
                        location = "مرکز همایش‌های بین‌المللی برج میلاد",
                        organizer = "ستاد ویژه توسعه نانو",
                        type = ElmEventType.CONGRESS,
                        isExternal = true,
                        link = "https://nano-congress2024.ir"
                    ),
                    ElmEvent(
                        title = "بیست و پنجمین سمینار ریاضی ایران",
                        description = "گرد هم‌آیی اساتید و دانشجویان مقاطع تحصیلات تکمیلی برای هم‌اندیشی در مبانی ریاضیات.",
                        date = "۱۰ خرداد ۱۴۰۳",
                        location = "دانشگاه صنعتی اصفهان",
                        organizer = "انجمن ریاضی ایران",
                        type = ElmEventType.CONGRESS,
                        isExternal = false
                    ),
                    ElmEvent(
                        title = "جشنواره فین‌تک برای همه",
                        description = "مسابقه ایده‌پردازی و برنامه‌نویسی در لبه تکنولوژی‌های مالی و بلاک‌چین.",
                        date = "۵ تیر ۱۴۰۳",
                        location = "کارخانه نوآوری آزادی",
                        organizer = "بانک مرکزی (رگ‌تک)",
                        reward = "استخدام در شرکت‌های برتر",
                        type = ElmEventType.COMPETITION
                    )
                )
                repository.saveAll(events)
            }
        }
    }
}
