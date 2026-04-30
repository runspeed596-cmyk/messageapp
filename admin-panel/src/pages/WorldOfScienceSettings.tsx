import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminApi, getMediaUrl } from '../api/adminApi';
import type { FieldOfStudy, EducationLevel, Faculty, EducationalRoleOption, ReferenceClub, ReferenceStudentOrg, HomeBanner } from '../api/adminApi';
import { Users, Image as ImageIcon, Upload } from 'lucide-react';
import {
    BookOpen, GraduationCap, Building2, Plus, Trash2, Check, X, Loader2,
    Pencil, ArrowRight, AlertTriangle, Shield, Layers
} from 'lucide-react';

type Tab = 'fields' | 'levels' | 'faculties' | 'roles' | 'clubs' | 'studentOrgs' | 'slider';

const WorldOfScienceSettings = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState<Tab>('fields');
    const [fieldsOfStudy, setFieldsOfStudy] = useState<FieldOfStudy[]>([]);
    const [educationLevels, setEducationLevels] = useState<EducationLevel[]>([]);
    const [faculties, setFaculties] = useState<Faculty[]>([]);
    const [educationalRoles, setEducationalRoles] = useState<EducationalRoleOption[]>([]);
    const [clubs, setClubs] = useState<ReferenceClub[]>([]);
    const [studentOrgs, setStudentOrgs] = useState<ReferenceStudentOrg[]>([]);
    const [sliderBanners, setSliderBanners] = useState<HomeBanner[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string>('');

    // Field form
    const [showFieldForm, setShowFieldForm] = useState<boolean>(false);
    const [fieldForm, setFieldForm] = useState<FieldOfStudy>({ name: '', educationLevel: '', displayOrder: 0 });
    const [editingFieldId, setEditingFieldId] = useState<string | null>(null);

    // Level form
    const [showLevelForm, setShowLevelForm] = useState<boolean>(false);
    const [levelForm, setLevelForm] = useState<EducationLevel>({ name: '', roleValueEn: '', displayOrder: 0 });
    const [editingLevelId, setEditingLevelId] = useState<string | null>(null);

    // Faculty form
    const [showFacultyForm, setShowFacultyForm] = useState<boolean>(false);
    const [facultyForm, setFacultyForm] = useState<Faculty>({ name: '', educationLevel: '', displayOrder: 0 });
    const [editingFacultyId, setEditingFacultyId] = useState<string | null>(null);

    // Role form
    const [showRoleForm, setShowRoleForm] = useState<boolean>(false);
    const [roleForm, setRoleForm] = useState<EducationalRoleOption>({ labelFa: '', valueEn: '', emoji: '', displayOrder: 0 });
    const [editingRoleId, setEditingRoleId] = useState<string | null>(null);

    // Club form
    const [showClubForm, setShowClubForm] = useState<boolean>(false);
    const [clubForm, setClubForm] = useState<ReferenceClub>({ name: '', displayOrder: 0 });

    // Student Org form
    const [showStudentOrgForm, setShowStudentOrgForm] = useState<boolean>(false);
    const [studentOrgForm, setStudentOrgForm] = useState<ReferenceStudentOrg>({ name: '', displayOrder: 0 });

    // Slider form
    const [showSliderForm, setShowSliderForm] = useState<boolean>(false);
    const [sliderTitle, setSliderTitle] = useState<string>('');
    const [sliderOrder, setSliderOrder] = useState<number>(0);
    const [sliderFile, setSliderFile] = useState<File | null>(null);
    const [sliderPreview, setSliderPreview] = useState<string>('');
    const [isSliderUploading, setIsSliderUploading] = useState<boolean>(false);
    const sliderFileRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        loadData();
    }, []);

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => setError(''), 4000);
            return () => clearTimeout(timer);
        }
    }, [error]);

    const loadData = async (): Promise<void> => {
        setLoading(true);
        try {
            const [fieldsRes, levelsRes, facultiesRes, rolesRes, clubsRes, orgsRes, slidersRes] = await Promise.all([
                adminApi.getFieldsOfStudy(),
                adminApi.getEducationLevels(),
                adminApi.getFaculties(),
                adminApi.getEducationalRoles(),
                adminApi.getClubs(),
                adminApi.getStudentOrgs(),
                adminApi.getMosbatElmBanners()
            ]);
            setFieldsOfStudy(fieldsRes.data.data || []);
            setEducationLevels(levelsRes.data.data || []);
            setFaculties(facultiesRes.data.data || []);
            setEducationalRoles(rolesRes.data.data || []);
            setClubs(clubsRes.data.data || []);
            setStudentOrgs(orgsRes.data.data || []);
            setSliderBanners(slidersRes.data.data || []);
        } catch (err) {
            console.error('Error loading data:', err);
        }
        setLoading(false);
    };

    // ─── Field of Study handlers ───
    const handleSaveField = async (): Promise<void> => {
        if (!fieldForm.name.trim()) return;
        if (!fieldForm.educationLevel) {
            setError('تعیین مقطع تحصیلی الزامی است');
            return;
        }
        try {
            const res = await adminApi.saveFieldOfStudy(fieldForm);
            if (!res.data.success) {
                setError(res.data.message || 'خطا در ذخیره رشته');
                return;
            }
            setShowFieldForm(false);
            setFieldForm({ name: '', educationLevel: '', displayOrder: 0 });
            setEditingFieldId(null);
            loadData();
        } catch (err: any) {
            const msg: string = err?.response?.data?.message || 'خطا در ذخیره رشته';
            setError(msg);
        }
    };

    const handleEditField = (field: FieldOfStudy): void => {
        setFieldForm({ id: field.id, name: field.name, educationLevel: field.educationLevel, displayOrder: field.displayOrder });
        setEditingFieldId(field.id!);
        setShowFieldForm(true);
    };

    const handleDeleteField = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این رشته مطمئن هستید؟')) return;
        try {
            await adminApi.deleteFieldOfStudy(id);
            loadData();
        } catch (err) {
            console.error('Error deleting field:', err);
        }
    };

    // ─── Education Level handlers ───
    const handleSaveLevel = async (): Promise<void> => {
        if (!levelForm.name.trim()) return;
        try {
            await adminApi.saveEducationLevel(levelForm);
            setShowLevelForm(false);
            setLevelForm({ name: '', roleValueEn: '', displayOrder: 0 });
            setEditingLevelId(null);
            loadData();
        } catch (err) {
            console.error('Error saving level:', err);
        }
    };

    const handleEditLevel = (level: EducationLevel): void => {
        setLevelForm({ id: level.id, name: level.name, roleValueEn: level.roleValueEn, displayOrder: level.displayOrder });
        setEditingLevelId(level.id!);
        setShowLevelForm(true);
    };

    const handleDeleteLevel = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این مقطع مطمئن هستید؟')) return;
        try {
            await adminApi.deleteEducationLevel(id);
            loadData();
        } catch (err) {
            console.error('Error deleting level:', err);
        }
    };

    // ─── Faculty handlers ───
    const handleSaveFaculty = async (): Promise<void> => {
        if (!facultyForm.name.trim()) return;
        try {
            const res = await adminApi.saveFaculty(facultyForm);
            if (!res.data.success) {
                setError(res.data.message || 'خطا در ذخیره دانشکده');
                return;
            }
            setShowFacultyForm(false);
            setFacultyForm({ name: '', educationLevel: '', displayOrder: 0 });
            setEditingFacultyId(null);
            loadData();
        } catch (err: any) {
            const msg: string = err?.response?.data?.message || 'خطا در ذخیره دانشکده';
            setError(msg);
        }
    };

    const handleEditFaculty = (faculty: Faculty): void => {
        setFacultyForm({ id: faculty.id, name: faculty.name, educationLevel: faculty.educationLevel, displayOrder: faculty.displayOrder });
        setEditingFacultyId(faculty.id!);
        setShowFacultyForm(true);
    };

    const handleDeleteFaculty = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این دانشکده مطمئن هستید؟')) return;
        try {
            await adminApi.deleteFaculty(id);
            loadData();
        } catch (err) {
            console.error('Error deleting faculty:', err);
        }
    };

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-black text-white">تنظیمات جهان علم</h1>
                    <p className="text-slate-400 mt-2">مدیریت رشته‌های تحصیلی، مقاطع و دانشکده‌ها</p>
                </div>
                <button
                    onClick={() => navigate('/universities')}
                    className="flex items-center gap-2 glass text-emerald-400 hover:text-emerald-300 hover:bg-emerald-500/10 border border-emerald-500/20 px-5 py-2.5 rounded-xl font-bold text-sm transition-all"
                >
                    بخش اصلی دانشگاه‌ها
                    <ArrowRight size={16} className="rotate-180" />
                </button>
            </div>

            {/* Error Toast */}
            {error && (
                <div className="fixed top-6 left-1/2 -translate-x-1/2 z-50 bg-rose-600/90 backdrop-blur-xl text-white px-6 py-3 rounded-2xl shadow-2xl shadow-rose-500/30 flex items-center gap-3 animate-in fade-in slide-in-from-top-4  duration-300 border border-rose-400/30">
                    <AlertTriangle size={18} />
                    <span className="font-bold text-sm">{error}</span>
                </div>
            )}

            {/* Tabs */}
            <div className="flex gap-3 flex-wrap">
                <button
                    onClick={() => setActiveTab('fields')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'fields'
                        ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <BookOpen size={18} />
                    رشته‌های تحصیلی
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{fieldsOfStudy.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('faculties')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'faculties'
                        ? 'bg-amber-600 text-white shadow-lg shadow-amber-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Building2 size={18} />
                    دانشکده‌ها
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{faculties.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('levels')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'levels'
                        ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <GraduationCap size={18} />
                    مقاطع تحصیلی
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{educationLevels.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('roles')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'roles'
                        ? 'bg-rose-600 text-white shadow-lg shadow-rose-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Users size={18} />
                    نقش‌های آموزشی
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{educationalRoles.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('clubs')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'clubs'
                        ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Shield size={18} />
                    کانون‌ها
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{clubs.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('studentOrgs')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'studentOrgs'
                        ? 'bg-cyan-600 text-white shadow-lg shadow-cyan-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Users size={18} />
                    تشکل‌های دانشجویی
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{studentOrgs.length}</span>
                </button>
                <button
                    onClick={() => setActiveTab('slider')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeTab === 'slider'
                        ? 'bg-orange-600 text-white shadow-lg shadow-orange-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Layers size={18} />
                    اسلایدر مثبت علم
                    <span className="bg-white/10 px-2 py-0.5 rounded-lg text-xs">{sliderBanners.length}</span>
                </button>
            </div>

            {loading && (
                <div className="flex justify-center py-20">
                    <Loader2 size={32} className="text-indigo-400 animate-spin" />
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Fields of Study Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'fields' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <BookOpen size={20} className="text-indigo-400" />
                            رشته‌های تحصیلی ({fieldsOfStudy.length})
                        </h2>
                        <button
                            onClick={() => { setShowFieldForm(true); setEditingFieldId(null); setFieldForm({ name: '', educationLevel: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن رشته
                        </button>
                    </div>

                    {showFieldForm && (
                        <div className="glass p-6 rounded-2xl border-emerald-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-emerald-400 mb-4">{editingFieldId ? '✏️ ویرایش رشته' : 'ثبت رشته جدید'}</h3>
                            <div className="flex gap-4 items-end flex-wrap">
                                <div className="flex-1 min-w-[200px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام رشته *</label>
                                    <input
                                        type="text"
                                        value={fieldForm.name}
                                        onChange={(e) => setFieldForm({ ...fieldForm, name: e.target.value })}
                                        placeholder="مثال: مهندسی کامپیوتر"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="w-48 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">مقطع تحصیلی *</label>
                                    <select
                                        value={fieldForm.educationLevel}
                                        onChange={(e) => setFieldForm({ ...fieldForm, educationLevel: e.target.value })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    >
                                        <option value="" className="bg-slate-900">انتخاب مقطع</option>
                                        {educationLevels
                                            .sort((a, b) => a.displayOrder - b.displayOrder)
                                            .map(level => (
                                                <option key={level.id} value={level.name} className="bg-slate-900">{level.name}</option>
                                            ))}
                                    </select>
                                </div>
                                <div className="w-32 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input
                                        type="number"
                                        value={fieldForm.displayOrder}
                                        onChange={(e) => setFieldForm({ ...fieldForm, displayOrder: parseInt(e.target.value) || 0 })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={handleSaveField}
                                        className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <Check size={16} />
                                        ذخیره
                                    </button>
                                    <button
                                        onClick={() => { setShowFieldForm(false); setFieldForm({ name: '', educationLevel: '', displayOrder: 0 }); setEditingFieldId(null); }}
                                        className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <X size={16} />
                                        انصراف
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام رشته</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">مقطع</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th>
                                </tr>
                            </thead>
                            <tbody>
                                {fieldsOfStudy
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((field, index) => (
                                        <tr key={field.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                            <td className="p-4 text-sm font-bold text-white">{field.name}</td>
                                            <td className="p-4 text-center">
                                                <span className="bg-indigo-500/10 text-indigo-400 px-3 py-1 rounded-lg text-xs font-bold">
                                                    {field.educationLevel || '—'}
                                                </span>
                                            </td>
                                            <td className="p-4 text-center text-sm text-slate-400">{field.displayOrder}</td>
                                            <td className="p-4 text-center">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => handleEditField(field)}
                                                        className="p-2 bg-indigo-500/10 text-indigo-400 hover:bg-indigo-500/20 rounded-lg transition-all"
                                                    >
                                                        <Pencil size={16} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteField(field.id!)}
                                                        className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                {fieldsOfStudy.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="p-12 text-center text-slate-600 text-sm">
                                            هنوز رشته‌ای ثبت نشده است
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Faculties Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'faculties' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <Building2 size={20} className="text-amber-400" />
                            دانشکده‌ها ({faculties.length})
                        </h2>
                        <button
                            onClick={() => { setShowFacultyForm(true); setEditingFacultyId(null); setFacultyForm({ name: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن دانشکده
                        </button>
                    </div>

                    {showFacultyForm && (
                        <div className="glass p-6 rounded-2xl border-amber-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-amber-400 mb-4">{editingFacultyId ? '✏️ ویرایش دانشکده' : 'ثبت دانشکده جدید'}</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 items-end">
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام دانشکده *</label>
                                    <input
                                        type="text"
                                        value={facultyForm.name}
                                        onChange={(e) => setFacultyForm({ ...facultyForm, name: e.target.value })}
                                        placeholder="مثال: فنی و مهندسی"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-amber-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">مقطع مرتبط *</label>
                                    <select
                                        value={facultyForm.educationLevel || ''}
                                        onChange={(e) => setFacultyForm({ ...facultyForm, educationLevel: e.target.value })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-amber-500 outline-none appearance-none"
                                    >
                                        <option value="" className="bg-slate-900">انتخاب مقطع...</option>
                                        {educationLevels.map(level => (
                                            <option key={level.id} value={level.name} className="bg-slate-900">
                                                {level.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input
                                        type="number"
                                        value={facultyForm.displayOrder}
                                        onChange={(e) => setFacultyForm({ ...facultyForm, displayOrder: parseInt(e.target.value) || 0 })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-amber-500 outline-none"
                                    />
                                </div>
                                <div className="flex gap-2 lg:col-span-3 justify-end pt-2">
                                    <button
                                        onClick={handleSaveFaculty}
                                        className="flex items-center gap-2 bg-amber-600 hover:bg-amber-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <Check size={16} />
                                        ذخیره
                                    </button>
                                    <button
                                        onClick={() => { setShowFacultyForm(false); setFacultyForm({ name: '', displayOrder: 0 }); setEditingFacultyId(null); }}
                                        className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <X size={16} />
                                        انصراف
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام دانشکده</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب نمایش</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th>
                                </tr>
                            </thead>
                            <tbody>
                                {faculties
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((faculty, index) => (
                                        <tr key={faculty.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                            <td className="p-4 text-sm font-bold text-white">{faculty.name}</td>
                                            <td className="p-4 text-center text-sm text-slate-400">{faculty.displayOrder}</td>
                                            <td className="p-4 text-center">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => handleEditFaculty(faculty)}
                                                        className="p-2 bg-amber-500/10 text-amber-400 hover:bg-amber-500/20 rounded-lg transition-all"
                                                    >
                                                        <Pencil size={16} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteFaculty(faculty.id!)}
                                                        className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                {faculties.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="p-12 text-center text-slate-600 text-sm">
                                            هنوز دانشکده‌ای ثبت نشده است
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Education Levels Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'levels' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <GraduationCap size={20} className="text-purple-400" />
                            مقاطع تحصیلی ({educationLevels.length})
                        </h2>
                        <button
                            onClick={() => { setShowLevelForm(true); setEditingLevelId(null); setLevelForm({ name: '', roleValueEn: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن مقطع
                        </button>
                    </div>

                    {showLevelForm && (
                        <div className="glass p-6 rounded-2xl border-purple-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-purple-400 mb-4">{editingLevelId ? '✏️ ویرایش مقطع' : 'ثبت مقطع جدید'}</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
                                <div className="space-y-2 col-span-1 md:col-span-2 lg:col-span-1">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام مقطع *</label>
                                    <input
                                        type="text"
                                        value={levelForm.name}
                                        onChange={(e) => setLevelForm({ ...levelForm, name: e.target.value })}
                                        placeholder="مثال: کارشناسی"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-purple-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">مربوط به نقش *</label>
                                    <select
                                        value={levelForm.roleValueEn || ''}
                                        onChange={(e) => setLevelForm({ ...levelForm, roleValueEn: e.target.value })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-purple-500 outline-none appearance-none"
                                    >
                                        <option value="" className="bg-slate-900">بدون نقش (عمومی)</option>
                                        {educationalRoles.map(role => (
                                            <option key={role.valueEn} value={role.valueEn} className="bg-slate-900">
                                                {role.emoji} {role.labelFa}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div className="w-full space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input
                                        type="number"
                                        value={levelForm.displayOrder}
                                        onChange={(e) => setLevelForm({ ...levelForm, displayOrder: parseInt(e.target.value) || 0 })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-purple-500 outline-none"
                                    />
                                </div>
                                </div>
                                <div className="flex gap-2 lg:col-span-4 justify-end pt-2">
                                    <button
                                        onClick={handleSaveLevel}
                                        className="flex items-center gap-2 bg-purple-600 hover:bg-purple-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <Check size={16} />
                                        ذخیره
                                    </button>
                                    <button
                                        onClick={() => { setShowLevelForm(false); setLevelForm({ name: '', roleValueEn: '', displayOrder: 0 }); setEditingLevelId(null); }}
                                        className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <X size={16} />
                                        انصراف
                                    </button>
                            </div>
                        </div>
                    )}

                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام مقطع</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نقش مرتبط</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب نمایش</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th>
                                </tr>
                            </thead>
                            <tbody>
                                {educationLevels
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((level, index) => (
                                        <tr key={level.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                            <td className="p-4 text-sm font-bold text-white">{level.name}</td>
                                            <td className="p-4 text-sm text-slate-400">
                                                {educationalRoles.find(r => r.valueEn === level.roleValueEn)?.labelFa || <span className="opacity-30">عمومی</span>}
                                            </td>
                                            <td className="p-4 text-center text-sm text-slate-400">{level.displayOrder}</td>
                                            <td className="p-4 text-center">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => handleEditLevel(level)}
                                                        className="p-2 bg-purple-500/10 text-purple-400 hover:bg-purple-500/20 rounded-lg transition-all"
                                                    >
                                                        <Pencil size={16} />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteLevel(level.id!)}
                                                        className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                {educationLevels.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="p-12 text-center text-slate-600 text-sm">
                                            هنوز مقطعی ثبت نشده است
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Educational Roles Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'roles' && !loading && (
                <div className="space-y-6">
                    <div className="flex items-center justify-between">
                        <h2 className="text-lg font-black text-white">
                            نقش‌های آموزشی ({educationalRoles.length})
                        </h2>
                        <button
                            onClick={() => { setShowRoleForm(true); setEditingRoleId(null); setRoleForm({ labelFa: '', valueEn: '', emoji: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-rose-600 hover:bg-rose-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن نقش
                        </button>
                    </div>

                    {showRoleForm && (
                        <div className="glass p-6 rounded-2xl border-rose-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-rose-400 mb-4">{editingRoleId ? '✏️ ویرایش نقش' : 'ثبت نقش جدید'}</h3>
                            <div className="flex gap-4 items-end flex-wrap">
                                <div className="flex-1 min-w-[180px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام فارسی *</label>
                                    <input
                                        type="text"
                                        value={roleForm.labelFa}
                                        onChange={(e) => setRoleForm({ ...roleForm, labelFa: e.target.value })}
                                        placeholder="مثال: دانش‌آموز"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-rose-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="flex-1 min-w-[180px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">مقدار انگلیسی *</label>
                                    <input
                                        type="text"
                                        value={roleForm.valueEn}
                                        onChange={(e) => setRoleForm({ ...roleForm, valueEn: e.target.value })}
                                        placeholder="مثال: SCHOOL_STUDENT"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-rose-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                                <div className="w-24 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ایموجی</label>
                                    <input
                                        type="text"
                                        value={roleForm.emoji}
                                        onChange={(e) => setRoleForm({ ...roleForm, emoji: e.target.value })}
                                        placeholder="🎒"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-rose-500 outline-none text-center text-lg"
                                    />
                                </div>
                                <div className="w-24 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input
                                        type="number"
                                        value={roleForm.displayOrder}
                                        onChange={(e) => setRoleForm({ ...roleForm, displayOrder: parseInt(e.target.value) || 0 })}
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-rose-500 outline-none"
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={async () => {
                                            if (!roleForm.labelFa.trim() || !roleForm.valueEn.trim()) return;
                                            try {
                                                await adminApi.saveEducationalRole(roleForm);
                                                setShowRoleForm(false);
                                                setRoleForm({ labelFa: '', valueEn: '', emoji: '', displayOrder: 0 });
                                                setEditingRoleId(null);
                                                loadData();
                                            } catch (err) { console.error('Error saving role:', err); }
                                        }}
                                        className="flex items-center gap-2 bg-rose-600 hover:bg-rose-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <Check size={16} />
                                        ذخیره
                                    </button>
                                    <button
                                        onClick={() => { setShowRoleForm(false); setRoleForm({ labelFa: '', valueEn: '', emoji: '', displayOrder: 0 }); setEditingRoleId(null); }}
                                        className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"
                                    >
                                        <X size={16} />
                                        انصراف
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-white/5">
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ایموجی</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام فارسی</th>
                                    <th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">مقدار انگلیسی</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب</th>
                                    <th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th>
                                </tr>
                            </thead>
                            <tbody>
                                {educationalRoles
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((role, index) => (
                                        <tr key={role.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                            <td className="p-4 text-xl">{role.emoji}</td>
                                            <td className="p-4 text-sm font-bold text-white">{role.labelFa}</td>
                                            <td className="p-4 text-sm text-slate-400 font-mono">{role.valueEn}</td>
                                            <td className="p-4 text-center text-sm text-slate-400">{role.displayOrder}</td>
                                            <td className="p-4 text-center">
                                                <div className="flex items-center justify-center gap-2">
                                                    <button
                                                        onClick={() => {
                                                            setRoleForm({ id: role.id, labelFa: role.labelFa, valueEn: role.valueEn, emoji: role.emoji, displayOrder: role.displayOrder });
                                                            setEditingRoleId(role.id!);
                                                            setShowRoleForm(true);
                                                        }}
                                                        className="p-2 rounded-lg bg-white/5 hover:bg-blue-500/20 text-slate-400 hover:text-blue-400 transition-all"
                                                    >
                                                        <Pencil size={14} />
                                                    </button>
                                                    <button
                                                        onClick={async () => {
                                                            if (role.id && confirm('آیا از حذف این نقش مطمئنید؟')) {
                                                                await adminApi.deleteEducationalRole(role.id);
                                                                loadData();
                                                            }
                                                        }}
                                                        className="p-2 rounded-lg bg-white/5 hover:bg-red-500/20 text-slate-400 hover:text-red-400 transition-all"
                                                    >
                                                        <Trash2 size={14} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                {educationalRoles.length === 0 && (
                                    <tr>
                                        <td colSpan={6} className="p-12 text-center text-slate-600 text-sm">
                                            هنوز نقشی ثبت نشده است
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Clubs Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'clubs' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <Shield size={20} className="text-teal-400" />
                            کانون‌ها ({clubs.length})
                        </h2>
                        <button
                            onClick={() => { setShowClubForm(true); setClubForm({ name: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن کانون
                        </button>
                    </div>
                    {showClubForm && (
                        <div className="glass p-6 rounded-2xl border-teal-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-teal-400 mb-4">ثبت کانون جدید</h3>
                            <div className="flex gap-4 items-end flex-wrap">
                                <div className="flex-1 min-w-[200px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام کانون *</label>
                                    <input type="text" value={clubForm.name} onChange={(e) => setClubForm({ ...clubForm, name: e.target.value })} placeholder="مثال: کانون فرهنگی" className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-teal-500 outline-none placeholder:text-slate-600" />
                                </div>
                                <div className="w-32 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input type="number" value={clubForm.displayOrder} onChange={(e) => setClubForm({ ...clubForm, displayOrder: parseInt(e.target.value) || 0 })} className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-teal-500 outline-none" />
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={async () => { if (!clubForm.name.trim()) return; try { await adminApi.saveClub(clubForm); setShowClubForm(false); setClubForm({ name: '', displayOrder: 0 }); loadData(); } catch (err) { setError('خطا در ذخیره کانون'); } }} className="flex items-center gap-2 bg-teal-600 hover:bg-teal-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"><Check size={16} />ذخیره</button>
                                    <button onClick={() => { setShowClubForm(false); setClubForm({ name: '', displayOrder: 0 }); }} className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"><X size={16} />انصراف</button>
                                </div>
                            </div>
                        </div>
                    )}
                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead><tr className="border-b border-white/5"><th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th><th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام کانون</th><th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب</th><th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th></tr></thead>
                            <tbody>
                                {clubs.sort((a, b) => a.displayOrder - b.displayOrder).map((club, index) => (
                                    <tr key={club.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                        <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                        <td className="p-4 text-sm font-bold text-white">{club.name}</td>
                                        <td className="p-4 text-center text-sm text-slate-400">{club.displayOrder}</td>
                                        <td className="p-4 text-center"><button onClick={async () => { if (club.id && confirm('آیا از حذف این کانون مطمئنید؟')) { await adminApi.deleteClub(club.id); loadData(); } }} className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"><Trash2 size={16} /></button></td>
                                    </tr>
                                ))}
                                {clubs.length === 0 && (<tr><td colSpan={4} className="p-12 text-center text-slate-600 text-sm">هنوز کانونی ثبت نشده است</td></tr>)}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Student Orgs Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'studentOrgs' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <Users size={20} className="text-cyan-400" />
                            تشکل‌های دانشجویی ({studentOrgs.length})
                        </h2>
                        <button
                            onClick={() => { setShowStudentOrgForm(true); setStudentOrgForm({ name: '', displayOrder: 0 }); }}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن تشکل
                        </button>
                    </div>
                    {showStudentOrgForm && (
                        <div className="glass p-6 rounded-2xl border-cyan-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-cyan-400 mb-4">ثبت تشکل جدید</h3>
                            <div className="flex gap-4 items-end flex-wrap">
                                <div className="flex-1 min-w-[200px] space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام تشکل *</label>
                                    <input type="text" value={studentOrgForm.name} onChange={(e) => setStudentOrgForm({ ...studentOrgForm, name: e.target.value })} placeholder="مثال: بسیج دانشجویی" className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-cyan-500 outline-none placeholder:text-slate-600" />
                                </div>
                                <div className="w-32 space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">ترتیب</label>
                                    <input type="number" value={studentOrgForm.displayOrder} onChange={(e) => setStudentOrgForm({ ...studentOrgForm, displayOrder: parseInt(e.target.value) || 0 })} className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-cyan-500 outline-none" />
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={async () => { if (!studentOrgForm.name.trim()) return; try { await adminApi.saveStudentOrg(studentOrgForm); setShowStudentOrgForm(false); setStudentOrgForm({ name: '', displayOrder: 0 }); loadData(); } catch (err) { setError('خطا در ذخیره تشکل'); } }} className="flex items-center gap-2 bg-cyan-600 hover:bg-cyan-500 text-white px-5 py-4 rounded-xl font-bold text-sm transition-all"><Check size={16} />ذخیره</button>
                                    <button onClick={() => { setShowStudentOrgForm(false); setStudentOrgForm({ name: '', displayOrder: 0 }); }} className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-4 rounded-xl font-bold text-sm transition-all"><X size={16} />انصراف</button>
                                </div>
                            </div>
                        </div>
                    )}
                    <div className="glass rounded-2xl overflow-hidden">
                        <table className="w-full">
                            <thead><tr className="border-b border-white/5"><th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">ردیف</th><th className="p-4 text-right text-xs font-black text-slate-500 uppercase tracking-widest">نام تشکل</th><th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب</th><th className="p-4 text-center text-xs font-black text-slate-500 uppercase tracking-widest">عملیات</th></tr></thead>
                            <tbody>
                                {studentOrgs.sort((a, b) => a.displayOrder - b.displayOrder).map((org, index) => (
                                    <tr key={org.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                        <td className="p-4 text-sm text-slate-600">{index + 1}</td>
                                        <td className="p-4 text-sm font-bold text-white">{org.name}</td>
                                        <td className="p-4 text-center text-sm text-slate-400">{org.displayOrder}</td>
                                        <td className="p-4 text-center"><button onClick={async () => { if (org.id && confirm('آیا از حذف این تشکل مطمئنید؟')) { await adminApi.deleteStudentOrg(org.id); loadData(); } }} className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all"><Trash2 size={16} /></button></td>
                                    </tr>
                                ))}
                                {studentOrgs.length === 0 && (<tr><td colSpan={4} className="p-12 text-center text-slate-600 text-sm">هنوز تشکلی ثبت نشده است</td></tr>)}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ═══════════════════════════════════════════════════════════════ */}
            {/* ─── Mosbat Elm Slider Tab ─── */}
            {/* ═══════════════════════════════════════════════════════════════ */}
            {activeTab === 'slider' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <Layers size={20} className="text-orange-400" />
                            اسلایدر مثبت علم ({sliderBanners.length})
                        </h2>
                        <button
                            onClick={() => { setShowSliderForm(true); setSliderTitle(''); setSliderOrder(0); setSliderFile(null); setSliderPreview(''); }}
                            className="flex items-center gap-3 bg-orange-600 hover:bg-orange-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            افزودن اسلاید
                        </button>
                    </div>
                    {showSliderForm && (
                        <div className="glass p-6 rounded-2xl border-orange-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-orange-400 mb-4">افزودن اسلاید مثبت علم (16:9)</h3>
                            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                                <div className="space-y-4">
                                    <label className="text-xs font-black text-slate-500 uppercase tracking-widest">آپلود تصویر بنر</label>
                                    <div onClick={() => sliderFileRef.current?.click()} className={`aspect-video rounded-2xl border-2 border-dashed transition-all cursor-pointer overflow-hidden ${sliderPreview ? 'border-orange-500/50 bg-orange-500/10' : 'border-white/10 hover:border-orange-500/30 bg-white/5 hover:bg-white/10'}`}>
                                        {sliderPreview ? (<img src={sliderPreview} alt="Preview" className="w-full h-full object-cover" />) : (
                                            <div className="w-full h-full flex flex-col items-center justify-center gap-4 text-slate-500"><Upload size={48} className="text-orange-400/50" /><p className="font-bold text-white/70">کلیک کنید یا تصویر را بکشید</p></div>
                                        )}
                                    </div>
                                    <input ref={sliderFileRef} type="file" accept="image/png,image/jpeg,image/webp" onChange={(e) => { const f = e.target.files?.[0]; if (f) { setSliderFile(f); setSliderPreview(URL.createObjectURL(f)); } }} className="hidden" />
                                </div>
                                <div className="space-y-4">
                                    <div className="space-y-2">
                                        <label className="text-xs font-black text-slate-500 uppercase tracking-widest">عنوان بنر</label>
                                        <input type="text" value={sliderTitle} onChange={(e) => setSliderTitle(e.target.value)} placeholder="مثال: ثبت‌نام کارگاه تابستانه" className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-orange-500 outline-none" />
                                    </div>
                                    <div className="space-y-2">
                                        <label className="text-xs font-black text-slate-500 uppercase tracking-widest">ترتیب نمایش</label>
                                        <input type="number" value={sliderOrder} onChange={(e) => setSliderOrder(parseInt(e.target.value) || 0)} className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-orange-500 outline-none" />
                                    </div>
                                </div>
                            </div>
                            <div className="flex gap-4 mt-6">
                                <button disabled={isSliderUploading || !sliderFile} onClick={async () => {
                                    if (!sliderFile) return;
                                    setIsSliderUploading(true);
                                    try {
                                        const imageUrl = await adminApi.uploadBannerImage(sliderFile);
                                        await adminApi.saveMosbatElmBanner({ title: sliderTitle, imageUrl, displayOrder: sliderOrder, isActive: true });
                                        setShowSliderForm(false); setSliderFile(null); setSliderPreview(''); setSliderTitle(''); setSliderOrder(0); loadData();
                                    } catch (err) { setError('خطا در آپلود یا ذخیره اسلاید'); } finally { setIsSliderUploading(false); }
                                }} className={`flex-1 text-white py-4 rounded-xl font-bold flex items-center justify-center gap-3 transition-all ${isSliderUploading || !sliderFile ? 'bg-slate-600 cursor-not-allowed' : 'bg-orange-600 hover:bg-orange-500'}`}>
                                    {isSliderUploading ? (<><Loader2 size={20} className="animate-spin" />در حال آپلود...</>) : (<><Check size={20} />تایید و ثبت اسلاید</>)}
                                </button>
                                <button disabled={isSliderUploading} onClick={() => { setShowSliderForm(false); setSliderFile(null); setSliderPreview(''); setSliderTitle(''); }} className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-xl font-bold flex items-center justify-center gap-3 transition-all"><X size={20} />انصراف</button>
                            </div>
                        </div>
                    )}
                    {sliderBanners.length > 0 ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {sliderBanners.map((banner) => (
                                <div key={banner.id} className="glass rounded-2xl overflow-hidden group border-white/5 hover:border-orange-500/30 transition-all duration-500 shadow-xl">
                                    <div className="aspect-video bg-slate-800 relative overflow-hidden">
                                        {banner.imageUrl ? (<img src={getMediaUrl(banner.imageUrl)} alt={banner.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />) : (
                                            <div className="w-full h-full flex items-center justify-center text-slate-700"><ImageIcon size={60} /></div>
                                        )}
                                        <div className="absolute inset-x-0 bottom-0 p-4 bg-gradient-to-t from-slate-900 to-transparent">
                                            <div className="flex justify-between items-end">
                                                <div className="text-xs font-black bg-orange-500 text-white px-3 py-1 rounded-full">اولویت {banner.displayOrder}</div>
                                                <button onClick={async () => { if (banner.id && confirm('آیا از حذف این اسلاید مطمئنید؟')) { await adminApi.deleteMosbatElmBanner(banner.id); loadData(); } }} className="bg-rose-500 hover:bg-rose-400 text-white p-3 rounded-2xl shadow-xl transition-all active:scale-90"><Trash2 size={16} /></button>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="p-4"><h3 className="text-lg font-black text-white">{banner.title || 'بدون عنوان'}</h3></div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="glass p-16 rounded-2xl text-center border-dashed border-white/10">
                            <Layers size={40} className="text-slate-600 mx-auto mb-4" />
                            <h3 className="text-xl font-black text-white">هیچ اسلایدی وجود ندارد</h3>
                            <p className="text-slate-500 mt-2">برای نمایش بنر در صفحه مثبت علم اپلیکیشن، حداقل یک اسلاید اضافه کنید.</p>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default WorldOfScienceSettings;
