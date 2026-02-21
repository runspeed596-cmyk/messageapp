import { useState } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
    Users,
    LayoutDashboard,
    LogOut,
    ShieldCheck,
    ChevronLeft,
    ChevronDown,
    Image,
    Film,
    Percent,
    Trophy,
    BookOpen,
    Megaphone,
    FolderOpen,
    Settings,
    Building2
} from 'lucide-react';

interface SidebarItemProps {
    to: string;
    icon: React.ReactNode;
    label: string;
}

const SidebarItem: React.FC<SidebarItemProps> = ({ to, icon, label }) => (
    <NavLink
        to={to}
        className={({ isActive }) =>
            `flex items-center justify-between group p-4 rounded-2xl transition-all duration-300 ${isActive
                ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
                : 'text-slate-400 hover:text-white hover:bg-white/5'
            }`
        }
    >
        <div className="flex items-center gap-4">
            <div className="transition-transform group-hover:scale-110">
                {icon}
            </div>
            <span className="text-sm font-bold">{label}</span>
        </div>
        <ChevronLeft size={14} className="opacity-40 group-hover:translate-x-[-4px] transition-transform" />
    </NavLink>
);

const CONTENT_ROUTES: string[] = ['/banners', '/universities', '/world-of-science-settings', '/entertainment', '/discounts', '/competitions'];

const Sidebar = () => {
    const { logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const isContentActive: boolean = CONTENT_ROUTES.some(r => location.pathname === r);
    const [contentOpen, setContentOpen] = useState<boolean>(isContentActive);

    const handleLogout = (): void => {
        logout();
        navigate('/login');
    };

    return (
        <div className="w-72 glass h-screen border-l border-white/10 flex flex-col fixed right-0 top-0 z-50 rtl font-[Vazirmatn]">
            <div className="p-8 flex items-center gap-4 border-b border-white/5">
                <div className="w-12 h-12 bg-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/20">
                    <ShieldCheck className="text-white w-7 h-7" />
                </div>
                <div>
                    <span className="text-lg font-black text-white block leading-none">کلاسوره</span>
                    <span className="text-[10px] text-indigo-400 font-bold uppercase tracking-[0.2em]">Super Admin</span>
                </div>
            </div>

            <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
                <SidebarItem to="/" icon={<LayoutDashboard size={20} />} label="پیشخوان" />
                <SidebarItem to="/users" icon={<Users size={20} />} label="کاربران" />

                {/* Collapsible Content Management */}
                <button
                    onClick={() => setContentOpen(!contentOpen)}
                    className={`flex items-center justify-between w-full p-4 rounded-2xl transition-all duration-300 group ${isContentActive && !contentOpen
                        ? 'bg-indigo-600/30 text-white'
                        : 'text-slate-400 hover:text-white hover:bg-white/5'
                        }`}
                >
                    <div className="flex items-center gap-4">
                        <div className="transition-transform group-hover:scale-110">
                            <FolderOpen size={20} />
                        </div>
                        <span className="text-sm font-bold">مدیریت محتوا</span>
                    </div>
                    <ChevronDown
                        size={14}
                        className={`opacity-40 transition-transform duration-300 ${contentOpen ? 'rotate-180' : ''}`}
                    />
                </button>

                {contentOpen && (
                    <div className="mr-4 space-y-1 border-r border-white/5 pr-2 animate-in fade-in slide-in-from-top-2 duration-300">
                        <SidebarItem to="/banners" icon={<Image size={18} />} label="بنر تبلیغاتی" />
                        <SidebarItem to="/universities" icon={<Building2 size={18} />} label="جهان علم — بخش اصلی" />
                        <SidebarItem to="/world-of-science-settings" icon={<Settings size={18} />} label="جهان علم — تنظیمات" />
                        <SidebarItem to="/entertainment" icon={<Film size={18} />} label="سرگرمی" />
                        <SidebarItem to="/discounts" icon={<Percent size={18} />} label="دنیای تخفیف" />
                        <SidebarItem to="/competitions" icon={<Trophy size={18} />} label="قله علم" />
                    </div>
                )}

                <div className="pt-8 pb-2 px-4 text-[10px] font-black text-white/20 uppercase tracking-[0.2em]">
                    تنظیمات سیستم
                </div>
                <SidebarItem to="/user-profiles" icon={<BookOpen size={20} />} label="پروفایل کاربران" />
                <SidebarItem to="/official-channels-groups" icon={<Megaphone size={20} />} label="کانال‌ها و گروه‌ها" />
                <SidebarItem to="/advertisements" icon={<Megaphone size={20} />} label="تبلیغات" />
            </nav>

            <div className="p-6 border-t border-white/5">
                <button
                    onClick={handleLogout}
                    className="flex items-center gap-4 text-rose-400 hover:text-white hover:bg-rose-500/10 w-full p-4 rounded-2xl transition-all duration-300 font-bold"
                >
                    <LogOut size={20} />
                    <span className="text-sm">خروج از پنل</span>
                </button>
            </div>
        </div>
    );
};

export default Sidebar;
