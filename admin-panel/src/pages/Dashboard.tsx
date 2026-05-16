import { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import {
    Users,
    TrendingUp,
    Eye,
    ShoppingBag,
    ArrowUpRight,
    ArrowDownRight,
    Activity,
    Calendar,
    Globe,
    ShieldAlert
} from 'lucide-react';

const DashboardCard = ({ title, value, icon: Icon, trend, trendValue, color }: any) => (
    <div className="glass p-6 rounded-[1.5rem] relative group overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-500">
        <div className={`absolute top-0 right-0 w-32 h-32 bg-${color}-500/10 rounded-full blur-3xl -mr-10 -mt-10 group-hover:scale-150 transition-transform duration-700`}></div>

        <div className="flex justify-between items-start relative z-10">
            <div className={`w-12 h-12 bg-${color}-500/20 rounded-2xl flex items-center justify-center text-${color}-400 mb-4`}>
                <Icon size={24} />
            </div>
            <div className={`flex items-center gap-1 text-xs font-bold ${trend === 'up' ? 'text-emerald-400' : 'text-rose-400'}`}>
                {trendValue}% {trend === 'up' ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
            </div>
        </div>

        <div className="relative z-10">
            <div className="text-slate-400 text-sm font-medium mb-1">{title}</div>
            <div className="text-3xl font-black text-white">{value}</div>
        </div>

        <div className="mt-4 pt-4 border-t border-white/5 flex items-center justify-between relative z-10">
            <div className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">آخرین ۲۴ ساعت</div>
            <div className={`w-2 h-2 rounded-full bg-${color}-500 animate-pulse`}></div>
        </div>
    </div>
);

const Dashboard = () => {
    const [userCount, setUserCount] = useState(0);

    const isSuperAdmin = localStorage.getItem('isSuperAdmin') === 'true';
    let permissions: string[] = [];
    try {
        permissions = JSON.parse(localStorage.getItem('permissions') || '[]');
    } catch(e) {}
    const hasPermission = isSuperAdmin || permissions.includes('DASHBOARD');

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await adminApi.getUsers(0, 1);
                if (response.data.success) {
                    setUserCount(response.data.data.totalElements);
                }
            } catch (error) {
                console.error('Error fetching dashboard stats:', error);
            }
        };
        fetchStats();
    }, []);

    if (!hasPermission) {
        return (
            <div className="flex flex-col items-center justify-center h-[60vh] space-y-4 rtl font-[Vazirmatn]">
                <div className="w-20 h-20 bg-rose-500/10 rounded-full flex items-center justify-center mb-6">
                    <ShieldAlert size={40} className="text-rose-500" />
                </div>
                <h1 className="text-3xl font-black text-white">دسترسی غیرمجاز</h1>
                <p className="text-slate-400">شما مجوز مشاهده پیشخوان را ندارید.</p>
            </div>
        );
    }

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-center mb-10">
                <div>
                    <h1 className="text-4xl font-black text-white tracking-tight">پیشخوان مدیریت</h1>
                    <p className="text-slate-400 mt-2">خلاصه وضعیت اپلیکیشن کلاسوره در یک نگاه</p>
                </div>
                <div className="flex gap-3">
                    <div className="glass px-4 py-2 rounded-xl flex items-center gap-3 text-sm text-slate-300">
                        <Calendar size={18} className="text-indigo-400" />
                        <span>جمعه، ۱۷ بهمن ۱۴۰۴</span>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <DashboardCard
                    title="کل کاربران"
                    value={userCount.toLocaleString('fa-IR')}
                    icon={Users}
                    trend="up"
                    trendValue="۱۲.۵"
                    color="indigo"
                />
                <DashboardCard
                    title="بازدید روزانه"
                    value="۱,۸۴۲+"
                    icon={Eye}
                    trend="up"
                    trendValue="۸.۲"
                    color="purple"
                />
                <DashboardCard
                    title="تراکنش‌های موفق"
                    value="۸۵"
                    icon={ShoppingBag}
                    trend="down"
                    trendValue="۳.۱"
                    color="emerald"
                />
                <DashboardCard
                    title="نرخ درگیری (CTR)"
                    value="۶۴٪"
                    icon={TrendingUp}
                    trend="up"
                    trendValue="۲۲"
                    color="rose"
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 glass p-8 rounded-[2rem] min-h-[400px]">
                    <div className="flex justify-between items-center mb-8">
                        <h2 className="text-xl font-bold flex items-center gap-3">
                            <Activity className="text-indigo-500" /> نمودار فعالیت کاربران
                        </h2>
                        <div className="flex gap-2">
                            <button className="text-xs bg-indigo-500 text-white px-3 py-1 rounded-full">هفتگی</button>
                            <button className="text-xs text-slate-500 hover:text-white transition-colors">ماهانه</button>
                        </div>
                    </div>

                    {/* Chart Placeholder */}
                    <div className="w-full h-64 flex items-end justify-between px-4 pb-4">
                        {[40, 65, 45, 90, 70, 85, 55, 75, 60, 95, 40, 60].map((h, i) => (
                            <div
                                key={i}
                                style={{ height: `${h}%` }}
                                className="w-4 bg-gradient-to-t from-indigo-600/20 to-indigo-500 rounded-full animate-in slide-in-from-bottom duration-1000 delay-[i*50]"
                            ></div>
                        ))}
                    </div>
                    <div className="flex justify-between mt-4 px-2 text-[10px] text-slate-600 font-bold uppercase tracking-widest">
                        <span>شنبه</span><span>یکشنبه</span><span>دوشنبه</span><span>سه‌شنبه</span><span>چهارشنبه</span><span>پنجشنبه</span><span>جمعه</span>
                    </div>
                </div>

                <div className="glass p-8 rounded-[2rem]">
                    <h2 className="text-xl font-bold mb-8 flex items-center gap-3">
                        <Globe className="text-purple-500" /> مناطق فعال
                    </h2>
                    <div className="space-y-6">
                        {[
                            { city: 'تهران', stat: '۴۵٪', color: 'indigo' },
                            { city: 'مشهد', stat: '۱۸٪', color: 'purple' },
                            { city: 'اصفهان', stat: '۱۲٪', color: 'emerald' },
                            { city: 'تبریز', stat: '۹٪', color: 'rose' },
                            { city: 'شیراز', stat: '۷٪', color: 'amber' },
                        ].map((item, i) => (
                            <div key={i} className="group">
                                <div className="flex justify-between text-sm mb-2">
                                    <span className="text-slate-300 group-hover:text-white transition-colors">{item.city}</span>
                                    <span className="font-bold">{item.stat}</span>
                                </div>
                                <div className="h-1.5 w-full bg-white/5 rounded-full overflow-hidden">
                                    <div
                                        className={`h-full bg-${item.color}-500 rounded-full transition-all duration-1000 delay-500`}
                                        style={{ width: item.stat }}
                                    ></div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
