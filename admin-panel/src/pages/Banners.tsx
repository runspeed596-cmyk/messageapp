import { useState, useEffect, useRef } from 'react';
import { adminApi, getMediaUrl } from '../api/adminApi';
import type { HomeBanner } from '../api/adminApi';
import { Plus, Trash2, Check, X, Image as ImageIcon, ExternalLink, AlertCircle, Upload, Loader2 } from 'lucide-react';

const Banners = () => {
    const [banners, setBanners] = useState<HomeBanner[]>([]);
    const [isAdding, setIsAdding] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [previewUrl, setPreviewUrl] = useState<string>('');
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [newBanner, setNewBanner] = useState<HomeBanner>({
        title: '',
        imageUrl: '',
        linkUrl: '',
        isActive: true
    });

    useEffect(() => {
        fetchBanners();
    }, []);

    const fetchBanners = async () => {
        try {
            const response = await adminApi.getBanners();
            if (response.data.success) {
                setBanners(response.data.data);
            }
        } catch (error) {
            console.error('Error fetching banners:', error);
        }
    };

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setSelectedFile(file);
            const objectUrl = URL.createObjectURL(file);
            setPreviewUrl(objectUrl);
        }
    };

    const handleSave = async () => {
        if (!selectedFile) {
            alert('لطفاً یک تصویر انتخاب کنید');
            return;
        }

        setIsUploading(true);
        try {
            // First upload the image
            const imageUrl = await adminApi.uploadBannerImage(selectedFile);
            console.log('ST_DEBUG: Uploaded banner URL:', imageUrl);

            // Then create the banner with the returned URL
            await adminApi.saveBanner({
                ...newBanner,
                imageUrl
            });

            fetchBanners();
            setIsAdding(false);
            setNewBanner({ title: '', imageUrl: '', linkUrl: '', isActive: true });
            setSelectedFile(null);
            setPreviewUrl('');
        } catch (error) {
            console.error('Upload error:', error);
            alert('خطا در آپلود تصویر یا ذخیره اسلاید');
        } finally {
            setIsUploading(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('آیا از حذف این اسلاید اطمینان دارید؟')) {
            try {
                await adminApi.deleteBanner(id);
                setBanners(banners.filter(b => b.id !== id));
            } catch (error) {
                alert('خطا در حذف اسلاید');
            }
        }
    };

    const resetForm = () => {
        setIsAdding(false);
        setNewBanner({ title: '', imageUrl: '', linkUrl: '', isActive: true });
        setSelectedFile(null);
        setPreviewUrl('');
    };

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-black text-white">مدیریت اسلایدر اصلی</h1>
                    <p className="text-slate-400 mt-2">کنترل بنرهای تبلیغاتی و اطلاعیه‌های صفحه اصلی اپلیکیشن</p>
                </div>
                <button
                    onClick={() => setIsAdding(true)}
                    className="flex items-center gap-3 bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-3 rounded-2xl font-black shadow-lg shadow-indigo-500/20 transition-all active:scale-95 group"
                >
                    <Plus size={20} className="group-hover:rotate-90 transition-transform" />
                    <span>افزودن اسلاید جدید</span>
                </button>
            </div>

            {isAdding && (
                <div className="glass p-8 rounded-[2rem] border-indigo-500/20 shadow-2xl animate-in fade-in slide-in-from-top-4 duration-500">
                    <div className="flex items-center gap-4 mb-8">
                        <div className="w-12 h-12 bg-indigo-500/20 rounded-2xl flex items-center justify-center text-indigo-400">
                            <Plus size={24} />
                        </div>
                        <h2 className="text-xl font-black text-white">افزودن اسلاید هوشمند (16:9)</h2>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        {/* Image Upload Section */}
                        <div className="space-y-4">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">آپلود تصویر بنر</label>
                            <div
                                onClick={() => fileInputRef.current?.click()}
                                className={`aspect-video rounded-2xl border-2 border-dashed transition-all cursor-pointer overflow-hidden
                                    ${previewUrl
                                        ? 'border-indigo-500/50 bg-indigo-500/10'
                                        : 'border-white/10 hover:border-indigo-500/30 bg-white/5 hover:bg-white/10'
                                    }`}
                            >
                                {previewUrl ? (
                                    <img src={previewUrl} alt="Preview" className="w-full h-full object-cover" />
                                ) : (
                                    <div className="w-full h-full flex flex-col items-center justify-center gap-4 text-slate-500">
                                        <Upload size={48} className="text-indigo-400/50" />
                                        <div className="text-center">
                                            <p className="font-bold text-white/70">کلیک کنید یا تصویر را بکشید</p>
                                            <p className="text-xs mt-1">PNG, JPG, WEBP - حداکثر 10MB</p>
                                        </div>
                                    </div>
                                )}
                            </div>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/png,image/jpeg,image/webp,image/gif"
                                onChange={handleFileSelect}
                                className="hidden"
                            />
                            {selectedFile && (
                                <div className="flex items-center gap-2 text-xs text-emerald-400 font-bold">
                                    <Check size={14} />
                                    <span>{selectedFile.name}</span>
                                </div>
                            )}
                        </div>

                        {/* Form Fields */}
                        <div className="space-y-6">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">عنوان بنر</label>
                                <input
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    placeholder="مثال: جشنواره زمستانه"
                                    value={newBanner.title}
                                    onChange={e => setNewBanner({ ...newBanner, title: e.target.value })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">لینک مقصد (اختیاری)</label>
                                <input
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    placeholder="/target-page"
                                    value={newBanner.linkUrl}
                                    onChange={e => setNewBanner({ ...newBanner, linkUrl: e.target.value })}
                                />
                            </div>

                        </div>
                    </div>

                    <div className="flex gap-4 mt-10">
                        <button
                            onClick={handleSave}
                            disabled={isUploading || !selectedFile}
                            className={`flex-1 text-white py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all
                                ${isUploading || !selectedFile
                                    ? 'bg-slate-600 cursor-not-allowed'
                                    : 'bg-indigo-600 hover:bg-indigo-500'
                                }`}
                        >
                            {isUploading ? (
                                <>
                                    <Loader2 size={20} className="animate-spin" />
                                    در حال آپلود...
                                </>
                            ) : (
                                <>
                                    <Check size={20} /> تایید و ثبت اسلاید
                                </>
                            )}
                        </button>
                        <button
                            onClick={resetForm}
                            disabled={isUploading}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all"
                        >
                            <X size={20} /> انصراف از عملیات
                        </button>
                    </div>
                </div>
            )}

            {banners.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {banners.map((banner) => (
                        <div key={banner.id} className="glass rounded-[2rem] overflow-hidden group border-white/5 hover:border-indigo-500/30 transition-all duration-500 shadow-xl">
                            <div className="aspect-video bg-slate-800 relative overflow-hidden">
                                {banner.imageUrl ? (
                                    <img src={getMediaUrl(banner.imageUrl)} alt={banner.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center text-slate-700">
                                        <ImageIcon size={60} />
                                    </div>
                                )}
                                <div className="absolute inset-x-0 bottom-0 p-6 bg-gradient-to-t from-slate-900 to-transparent">
                                    <div className="flex justify-between items-end">
                                        <div className="text-xs font-black bg-indigo-500 text-white px-3 py-1 rounded-full mb-3">{banner.title}</div>
                                        <div className="flex gap-2">
                                            <button
                                                onClick={() => handleDelete(banner.id!)}
                                                className="bg-rose-500 hover:bg-rose-400 text-white p-3 rounded-2xl shadow-xl transition-all active:scale-90"
                                            >
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="p-6">
                                <h3 className="text-xl font-black text-white mb-2">{banner.title}</h3>
                                {banner.linkUrl ? (
                                    <div className="flex items-center gap-2 text-xs text-slate-500 font-bold">
                                        <ExternalLink size={12} />
                                        <span>لینک مقصد: {banner.linkUrl}</span>
                                    </div>
                                ) : (
                                    <div className="text-xs text-slate-600 font-bold italic">بدون لینک مقصد</div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="glass p-20 rounded-[2rem] text-center border-dashed border-white/10">
                    <div className="w-20 h-20 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-6">
                        <AlertCircle size={40} className="text-slate-600" />
                    </div>
                    <h3 className="text-2xl font-black text-white">هیچ اسلایدی وجود ندارد</h3>
                    <p className="text-slate-500 mt-2">برای لود شدن محتوا در صفحه اصلی اپلیکیشن، حداقل یک اسلاید اضافه کنید.</p>
                </div>
            )}
        </div>
    );
};

export default Banners;
