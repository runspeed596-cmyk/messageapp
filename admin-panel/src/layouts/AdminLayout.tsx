import React, { useState } from 'react';
import Sidebar from '../components/Sidebar';
import { Menu, ShieldCheck } from 'lucide-react';

const AdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [sidebarOpen, setSidebarOpen] = useState<boolean>(false);
    return (
        <div className="flex min-h-screen rtl font-[Vazirmatn]">
            {/* Sidebar */}
            <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
            {/* Main Content Area */}
            <main className="flex-1 min-w-0 max-w-full overflow-x-hidden md:mr-72 mr-0 relative z-10 transition-all duration-300">
                {/* Mobile Top Bar */}
                <div className="md:hidden sticky top-0 z-30 glass border-b border-white/10 px-4 py-3 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 bg-indigo-600 rounded-lg flex items-center justify-center shadow-lg shadow-indigo-500/20">
                            <ShieldCheck className="text-white w-5 h-5" />
                        </div>
                        <div>
                            <span className="text-sm font-black text-white block leading-none">کلاسوره</span>
                            <span className="text-[8px] text-indigo-400 font-bold uppercase tracking-[0.15em]">Super Admin</span>
                        </div>
                    </div>
                    <button
                        onClick={() => setSidebarOpen(true)}
                        className="p-2.5 rounded-xl glass-hover text-slate-300 hover:text-white"
                        aria-label="Open menu"
                    >
                        <Menu size={22} />
                    </button>
                </div>
                {/* Page Content */}
                <div className="p-4 md:p-10">
                    {children}
                </div>
            </main>
            {/* Background patterns */}
            <div className="fixed inset-0 pointer-events-none z-0">
                <div className="absolute top-0 right-1/4 w-[800px] h-[800px] bg-indigo-500/5 rounded-full blur-[150px]"></div>
                <div className="absolute bottom-0 left-1/4 w-[600px] h-[600px] bg-purple-500/5 rounded-full blur-[150px]"></div>
            </div>
        </div>
    );
};

export default AdminLayout;
