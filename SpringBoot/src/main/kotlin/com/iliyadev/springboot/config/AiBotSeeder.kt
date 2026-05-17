package com.iliyadev.springboot.config

import com.iliyadev.springboot.models.AiBot
import com.iliyadev.springboot.repositories.AiBotRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class AiBotSeeder(
    private val aiBotRepository: AiBotRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val botsToSeed: List<AiBot> = listOf(
            // ── مدل‌های عمومی ──
            AiBot(name = "ChatGPT", botType = "chatgpt", category = "GENERAL", description = "مدل هوش مصنوعی OpenAI"),
            AiBot(name = "Gemini", botType = "gemini", category = "GENERAL", description = "مدل هوش مصنوعی Google"),
            AiBot(name = "DeepSeek", botType = "deepseek", category = "GENERAL", description = "مدل هوش مصنوعی DeepSeek"),
            AiBot(name = "Grok", botType = "grok", category = "GENERAL", description = "مدل هوش مصنوعی xAI"),
            AiBot(name = "Copilot", botType = "copilot", category = "GENERAL", description = "مدل هوش مصنوعی Microsoft"),
            // ── دستیارهای تخصصی ──
            AiBot(name = "دستیار تخصصی امتحان", botType = "exam_assistant", category = "SPECIALIST", description = "کمک در آمادگی برای امتحانات"),
            AiBot(name = "دستیار تخصصی ترجمه", botType = "translation_assistant", category = "SPECIALIST", description = "ترجمه متون تخصصی و عمومی"),
            AiBot(name = "دستیار تخصصی نگارش مقاله", botType = "article_assistant", category = "SPECIALIST", description = "کمک در نگارش مقالات علمی"),
            AiBot(name = "دستیار تخصصی تحلیل فایل", botType = "file_analysis", category = "SPECIALIST", description = "تحلیل و بررسی فایل‌ها ⭐"),
            AiBot(name = "دستیار تخصصی تولید تصویر", botType = "image_generation", category = "SPECIALIST", description = "تولید تصاویر با هوش مصنوعی ⭐"),
            AiBot(name = "دستیار تخصصی ساخت پاورپوینت", botType = "powerpoint_assistant", category = "SPECIALIST", description = "ساخت خودکار پاورپوینت ⭐"),
            AiBot(name = "دستیار تخصصی ساخت کلیپ علمی", botType = "clip_assistant", category = "SPECIALIST", description = "ساخت کلیپ‌های علمی ⭐"),
            AiBot(name = "جستجوی مقالات", botType = "paper_search", category = "SPECIALIST", description = "جستجو در سایت‌های ایرانی + Sci-Hub ⭐"),
            AiBot(
                name = "ربات مثبت علم",
                botType = "mosbat_elm",
                category = "SPECIALIST",
                description = "دستیار هوشمند شما در دوره‌های آموزشی مثبت علم 🎓",
                                avatarUrl = "https://img.icons8.com/fluency/96/graduation-cap.png"
            )
        )
        
        botsToSeed.forEach { bot ->
            if (aiBotRepository.findByBotType(bot.botType) == null) {
                try {
                    aiBotRepository.save(bot)
                } catch (e: Exception) {
                    // Log and continue
                }
            }
        }
        
        // Final fallback for Mosbat Elm bot specifically
        if (aiBotRepository.findByBotType("mosbat_elm") == null) {
            aiBotRepository.save(AiBot(
                name = "ربات مثبت علم",
                botType = "mosbat_elm",
                category = "SPECIALIST",
                description = "دستیار هوشمند شما در دوره‌های آموزشی مثبت علم 🎓",
                                avatarUrl = "https://img.icons8.com/fluency/96/graduation-cap.png"
            ))
        }
    }
}
