import { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import type { Discount } from '../api/adminApi';
import { Plus, Check, X, Trash2, Percent, Copy, CheckCircle, Tag, AlertCircle, Loader2 } from 'lucide-react';

const Discounts = () => {
    const [discounts, setDiscounts] = useState<Discount[]>([]);
    const [isAdding, setIsAdding] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [copiedId, setCopiedId] = useState<string | null>(null);

    const [newDiscount, setNewDiscount] = useState<Partial<Discount>>({
        title: '',
        code: '',
        percent: 10,
        description: '',
        imageUrl: '',
        expiryDate: '',
        category: ''
    });

    const categories = [
        'غذا و رستوران',
        'سینما و تفریح',
        'ورزشی',
        'آموزشی',
        'فروشگاهی',
        'سفر و گردشگری',
        'پوشاک',
        'تکنولوژی',
        'سایر'
    ];

    useEffect(() => {
        fetchDiscounts();
    }, []);

    const fetchDiscounts = async () => {
        try {
            const response = await adminApi.getDiscounts();
            if (response.data.success) {
                setDiscounts(response.data.data);
            }
        } catch (error) {
            console.error('Error fetching discounts:', error);
        }
    };

    const handleSave = async () => {
        if (!newDiscount.title || !newDiscount.code) {
            alert('لطفاً عنوان و کد تخفیف را وارد کنید');
            return;
        }

        setIsLoading(true);
        try {
            const dataToSave = {
                ...newDiscount,
                percent: Number(newDiscount.percent) || 0,
                expiryDate: newDiscount.expiryDate || null
            };
            await adminApi.saveDiscount(dataToSave as Discount);
            fetchDiscounts();
            setIsAdding(false);
            setNewDiscount({
                title: '',
                code: '',
                percent: 10,
                description: '',
                imageUrl: '',
                expiryDate: '',
                category: ''
            });
        } catch (error) {
            alert('خطا در ذخیره تخفیف');
        } finally {
            setIsLoading(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('آیا از حذف این کد تخفیف اطمینان دارید؟')) {
            try {
                await adminApi.deleteDiscount(id);
                setDiscounts(discounts.filter(d => d.id !== id));
            } catch (error) {
                alert('خطا در حذف تخفیف');
            }
        }
    };

    const handleCopyCode = (code: string, id: string) => {
        navigator.clipboard.writeText(code).then(() => {
            setCopiedId(id);
            setTimeout(() => setCopiedId(null), 2000);
        });
    };

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-black text-white">دنیای تخفیف</h1>
                    <p className="text-slate-400 mt-2">مدیریت کدهای تخفیف قابل کپی توسط کاربران</p>
                </div>
                <button
                    onClick={() => setIsAdding(true)}
                    className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-6 py-3 rounded-2xl font-black shadow-lg shadow-emerald-500/20 transition-all active:scale-95 group"
                >
                    <Plus size={20} className="group-hover:rotate-90 transition-transform" />
                    <span>ثبت کد تخفیف جدید</span>
                </button>
            </div>

            {isAdding && (
                <div className="glass p-8 rounded-[2rem] border-emerald-500/20 shadow-2xl animate-in fade-in slide-in-from-top-4 duration-500">
                    <div className="flex items-center gap-4 mb-8">
                        <div className="w-12 h-12 bg-emerald-500/20 rounded-2xl flex items-center justify-center text-emerald-400">
                            <Tag size={24} />
                        </div>
                        <h2 className="text-xl font-black text-white">ایجاد کد تخفیف قابل کپی</h2>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <div className="space-y-2 lg:col-span-2">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">عنوان تخفیف</label>
                            <input
                                className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none"
                                placeholder="مثال: تخفیف ویژه فست فود"
                                value={newDiscount.title}
                                onChange={e => setNewDiscount({ ...newDiscount, title: e.target.value })}
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">درصد تخفیف</label>
                            <div className="flex items-center gap-2">
                                <input
                                    type="number"
                                    min="1" max="100"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none"
                                    value={newDiscount.percent}
                                    onChange={e => setNewDiscount({ ...newDiscount, percent: parseInt(e.target.value) })}
                                />
                                <span className="text-2xl font-black text-emerald-400">%</span>
                            </div>
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest flex items-center gap-2">
                                کد تخفیف
                                <span className="text-rose-400">(الزامی)</span>
                            </label>
                            <input
                                className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none font-mono text-lg tracking-wider"
                                placeholder="مثال: SAVE20"
                                value={newDiscount.code}
                                onChange={e => setNewDiscount({ ...newDiscount, code: e.target.value.toUpperCase() })}
                            />
                            <p className="text-xs text-slate-600">کاربران این کد را کپی می‌کنند</p>
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">دسته‌بندی</label>
                            <select
                                className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none appearance-none"
                                value={newDiscount.category}
                                onChange={e => setNewDiscount({ ...newDiscount, category: e.target.value })}
                            >
                                <option value="" className="bg-slate-900">انتخاب دسته‌بندی...</option>
                                {categories.map(c => <option key={c} className="bg-slate-900">{c}</option>)}
                            </select>
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تاریخ انقضا</label>
                            <input
                                type="date"
                                className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none"
                                value={newDiscount.expiryDate}
                                onChange={e => setNewDiscount({ ...newDiscount, expiryDate: e.target.value })}
                            />
                        </div>
                        <div className="space-y-2 lg:col-span-3">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">توضیحات (اختیاری)</label>
                            <textarea
                                className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none resize-none h-20"
                                placeholder="توضیحات اضافی درباره نحوه استفاده از تخفیف..."
                                value={newDiscount.description}
                                onChange={e => setNewDiscount({ ...newDiscount, description: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="flex gap-4 mt-10">
                        <button
                            onClick={handleSave}
                            disabled={isLoading}
                            className={`flex-1 text-white py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all
                                ${isLoading ? 'bg-slate-600 cursor-not-allowed' : 'bg-emerald-600 hover:bg-emerald-500'}`}
                        >
                            {isLoading ? (
                                <><Loader2 size={20} className="animate-spin" /> در حال ذخیره...</>
                            ) : (
                                <><Check size={20} /> ثبت کد تخفیف</>
                            )}
                        </button>
                        <button
                            onClick={() => setIsAdding(false)}
                            disabled={isLoading}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all"
                        >
                            <X size={20} /> انصراف
                        </button>
                    </div>
                </div>
            )}

            {discounts.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {discounts.map((discount) => (
                        <div key={discount.id} className="glass rounded-[2rem] p-6 border-white/5 hover:border-emerald-500/30 transition-all group">
                            <div className="flex justify-between items-start mb-4">
                                <div className="w-16 h-16 bg-emerald-500/20 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform">
                                    <Percent size={32} className="text-emerald-400" />
                                </div>
                                <div className="flex items-center gap-2">
                                    <span className="text-3xl font-black text-emerald-400">{discount.percent}%</span>
                                </div>
                            </div>

                            <h3 className="font-black text-lg text-white mb-2">{discount.title}</h3>
                            {discount.description && (
                                <p className="text-xs text-slate-500 mb-4 line-clamp-2">{discount.description}</p>
                            )}

                            {/* Copyable Code Section */}
                            <div className="bg-slate-800/50 rounded-xl p-4 mb-4">
                                <div className="flex items-center justify-between gap-2">
                                    <div>
                                        <p className="text-xs text-slate-500 mb-1">کد تخفیف:</p>
                                        <p className="font-mono text-xl font-black text-emerald-400 tracking-widest">
                                            {discount.code}
                                        </p>
                                    </div>
                                    <button
                                        onClick={() => handleCopyCode(discount.code, discount.id!)}
                                        className={`p-3 rounded-xl transition-all ${copiedId === discount.id
                                            ? 'bg-emerald-500 text-white'
                                            : 'bg-white/5 text-slate-400 hover:text-emerald-400 hover:bg-emerald-500/10'
                                            }`}
                                    >
                                        {copiedId === discount.id ? (
                                            <CheckCircle size={20} />
                                        ) : (
                                            <Copy size={20} />
                                        )}
                                    </button>
                                </div>
                            </div>

                            <div className="flex justify-between items-center text-xs text-slate-500">
                                <div className="flex items-center gap-2">
                                    <Tag size={12} />
                                    <span>{discount.category || 'بدون دسته'}</span>
                                </div>
                                <button
                                    onClick={() => handleDelete(discount.id!)}
                                    className="p-2 text-slate-400 hover:text-rose-400 transition-colors"
                                >
                                    <Trash2 size={16} />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="glass p-20 rounded-[2rem] text-center border-dashed border-white/10">
                    <div className="w-20 h-20 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-6">
                        <AlertCircle size={40} className="text-slate-600" />
                    </div>
                    <h3 className="text-2xl font-black text-white">هیچ کد تخفیفی ثبت نشده است</h3>
                    <p className="text-slate-500 mt-2">برای ارائه تخفیف به کاربران، یک کد جدید ثبت کنید.</p>
                </div>
            )}
        </div>
    );
};

export default Discounts;
