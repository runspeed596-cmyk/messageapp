import React from 'react';
import Sidebar from '../components/Sidebar';

const AdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    return (
        <div className="flex min-h-screen rtl font-[Vazirmatn]">
            {/* Sidebar - Fixed on the right for RTL */}
            <Sidebar />

            {/* Main Content Area */}
            <main className="flex-1 mr-72 p-10 relative z-10 transition-all duration-500">
                {children}
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
