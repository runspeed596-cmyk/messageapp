import { useNavigate } from 'react-router-dom';
import { BookOpen, ArrowRight } from 'lucide-react';

const UserProfiles = () => {
    const navigate = useNavigate();

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-black text-white">پروفایل کاربران</h1>
                <p className="text-slate-400 mt-2">تنظیمات مربوط به پروفایل کاربران</p>
            </div>

            {/* Info Note — redirect to World of Science Settings */}
            <div className="glass p-6 rounded-2xl border-blue-500/20 space-y-4">
                <div className="flex items-start gap-3">
                    <span className="text-blue-400 text-lg mt-0.5">ℹ️</span>
                    <div className="text-sm text-slate-400 leading-relaxed">
                        <p>
                            مدیریت <strong className="text-white">رشته‌های تحصیلی</strong>، <strong className="text-white">مقاطع تحصیلی</strong> و <strong className="text-white">دانشکده‌ها</strong> به بخش{' '}
                            <strong className="text-emerald-400 cursor-pointer hover:underline" onClick={() => navigate('/world-of-science-settings')}>تنظیمات جهان علم</strong> منتقل شده است.
                        </p>
                        <p className="mt-2">
                            اطلاعات <strong className="text-white">دانشگاه</strong> نیز از بخش{' '}
                            <strong className="text-emerald-400 cursor-pointer hover:underline" onClick={() => navigate('/universities')}>جهان علم — بخش اصلی</strong> بارگذاری می‌شود.
                        </p>
                    </div>
                </div>

                <div className="flex gap-3 flex-wrap">
                    <button
                        onClick={() => navigate('/world-of-science-settings')}
                        className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                    >
                        <BookOpen size={16} />
                        تنظیمات جهان علم
                        <ArrowRight size={14} className="rotate-180" />
                    </button>
                    <button
                        onClick={() => navigate('/universities')}
                        className="flex items-center gap-2 glass text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 border border-emerald-500/20 px-5 py-2.5 rounded-xl font-bold text-sm transition-all"
                    >
                        دانشگاه‌ها (بخش اصلی)
                        <ArrowRight size={14} className="rotate-180" />
                    </button>
                </div>
            </div>
        </div>
    );
};

export default UserProfiles;
