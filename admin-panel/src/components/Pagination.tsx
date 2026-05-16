import React from 'react';
import { ChevronRight, ChevronLeft, ChevronsRight, ChevronsLeft } from 'lucide-react';

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
    onPageChange: (page: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({
    currentPage, totalPages, totalElements, pageSize, onPageChange
}) => {
    if (totalPages <= 1) return null;
    const startItem: number = currentPage * pageSize + 1;
    const endItem: number = Math.min((currentPage + 1) * pageSize, totalElements);
    const getVisiblePages = (): number[] => {
        const pages: number[] = [];
        const maxVisible: number = 5;
        let start: number = Math.max(0, currentPage - Math.floor(maxVisible / 2));
        let end: number = Math.min(totalPages - 1, start + maxVisible - 1);
        if (end - start < maxVisible - 1) {
            start = Math.max(0, end - maxVisible + 1);
        }
        for (let i: number = start; i <= end; i++) {
            pages.push(i);
        }
        return pages;
    };
    const visiblePages: number[] = getVisiblePages();
    return (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6 px-2">
            {/* Info Text */}
            <div className="text-xs text-slate-400 font-bold tracking-wide">
                نمایش <span className="text-cyan-400">{startItem}</span> تا <span className="text-cyan-400">{endItem}</span> از <span className="text-emerald-400 font-black">{totalElements.toLocaleString('fa-IR')}</span> مورد
            </div>
            {/* Page Buttons */}
            <div className="flex items-center gap-1.5">
                {/* First */}
                <button
                    onClick={() => onPageChange(0)}
                    disabled={currentPage === 0}
                    className="p-1.5 rounded-lg transition-all duration-200 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 text-slate-400 hover:text-white"
                    title="صفحه اول"
                >
                    <ChevronsRight size={16} />
                </button>
                {/* Previous */}
                <button
                    onClick={() => onPageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                    className="p-1.5 rounded-lg transition-all duration-200 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 text-slate-400 hover:text-white"
                    title="صفحه قبل"
                >
                    <ChevronRight size={16} />
                </button>
                {/* Page Numbers */}
                {visiblePages[0] > 0 && (
                    <span className="text-slate-600 text-xs px-1">…</span>
                )}
                {visiblePages.map((page: number) => (
                    <button
                        key={page}
                        onClick={() => onPageChange(page)}
                        className={`min-w-[32px] h-8 rounded-lg text-xs font-black transition-all duration-200 ${
                            page === currentPage
                                ? 'bg-gradient-to-br from-cyan-500 to-blue-600 text-white shadow-lg shadow-cyan-500/30 scale-110'
                                : 'text-slate-400 hover:bg-white/10 hover:text-white'
                        }`}
                    >
                        {(page + 1).toLocaleString('fa-IR')}
                    </button>
                ))}
                {visiblePages[visiblePages.length - 1] < totalPages - 1 && (
                    <span className="text-slate-600 text-xs px-1">…</span>
                )}
                {/* Next */}
                <button
                    onClick={() => onPageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="p-1.5 rounded-lg transition-all duration-200 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 text-slate-400 hover:text-white"
                    title="صفحه بعد"
                >
                    <ChevronLeft size={16} />
                </button>
                {/* Last */}
                <button
                    onClick={() => onPageChange(totalPages - 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="p-1.5 rounded-lg transition-all duration-200 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 text-slate-400 hover:text-white"
                    title="صفحه آخر"
                >
                    <ChevronsLeft size={16} />
                </button>
            </div>
        </div>
    );
};

export default Pagination;
