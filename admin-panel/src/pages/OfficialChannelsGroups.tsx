import React, { useState, useEffect, useCallback } from 'react';
import { adminApi } from '../api/adminApi';
import type {
    OfficialChannel, OfficialGroup, AdminUser,
    OfficialChannelCategory, OfficialGroupCategory,
    CreateOfficialChannelRequest, CreateOfficialGroupRequest,
    FieldOfStudy, EducationLevel, University
} from '../api/adminApi';
import {
    Megaphone, Users, Plus, Trash2, Check, X, Loader2,
    UserPlus, UserMinus, Search, Shield, Eye, EyeOff, ChevronDown, ChevronUp, Globe, MapPin, Pencil
} from 'lucide-react';

type Section = 'channels' | 'groups';

const CHANNEL_CATEGORIES: { value: OfficialChannelCategory; label: string }[] = [
    { value: 'STUDENTS_IRAN', label: 'کانال رسمی دانشجویان ایران زمین' },
    { value: 'MY_FIELD', label: 'کانال کشوری رشته من' },
    { value: 'MY_UNIVERSITY', label: 'کانال رسمی دانشگاه من' },
    { value: 'MY_UNION', label: 'کانال رسمی شورای صنفی من' },
    { value: 'FREELANCING', label: 'کانال رسمی فریلنسری دانشجویی' },
    { value: 'PODCAST', label: 'کانال رسمی پادکست دانشجویی' },
    { value: 'JOURNAL', label: 'کانال رسمی نشریه دانشجویی' },
    { value: 'RESEARCH', label: 'کانال رسمی پروژه‌های تحقیقاتی' },
    { value: 'COMPETITIONS', label: 'کانال مسابقات، جشنواره‌ها و کنگره‌ها' },
    { value: 'SCIENCE_TECH', label: 'کانال رسمی علم + تکنولوژی' },
    { value: 'EDUCATION', label: 'کانال رسمی آموزش' },
    { value: 'STUDENT_NEWS', label: 'کانال اخبار دانشجویی کلاسور' },
    { value: 'ENTERTAINMENT', label: 'کانال تفریح و سرگرمی' },
    { value: 'APP_OFFICIAL', label: 'کانال رسمی اپلیکیشن کلاسور' },
    { value: 'LOTTERY_DISCOUNT', label: 'کانال رسمی قرعه‌کشی و تخفیفات' },
    { value: 'TEACHERS', label: 'کانال دبیران' },
    { value: 'QA_SCIENCE', label: 'کانال پرسش و پاسخ علمی' },
    { value: 'COURSE_GROUP', label: 'گروه/کانال دوره آموزشی' },
];

const GROUP_CATEGORIES: { value: OfficialGroupCategory; label: string }[] = [
    { value: 'STUDENTS_IRAN', label: 'گروه رسمی دانشجویان ایران زمین' },
    { value: 'MY_FIELD', label: 'گروه کشوری رشته من' },
    { value: 'MY_UNIVERSITY', label: 'گروه رسمی دانشگاه من' },
    { value: 'MY_FIELD_UNIVERSITY', label: 'گروه رسمی رشته دانشگاه من' },
    { value: 'MY_UNION', label: 'گروه رسمی شورای صنفی من' },
    { value: 'TEACHERS', label: 'گروه دبیران' },
    { value: 'QA_SCIENCE', label: 'گروه پرسش و پاسخ علمی' },
    { value: 'COURSE_GROUP', label: 'گروه دوره آموزشی' },
];

const getCategoryLabel = (categoryValue: string, type: 'channel' | 'group'): string => {
    if (type === 'channel') {
        const found = CHANNEL_CATEGORIES.find(c => c.value === categoryValue);
        return found ? found.label : categoryValue;
    }
    const found = GROUP_CATEGORIES.find(c => c.value === categoryValue);
    return found ? found.label : categoryValue;
};

const OfficialChannelsGroups: React.FC = () => {
    const [activeSection, setActiveSection] = useState<Section>('channels');
    const [channels, setChannels] = useState<OfficialChannel[]>([]);
    const [groups, setGroups] = useState<OfficialGroup[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [showChannelForm, setShowChannelForm] = useState<boolean>(false);
    const [showGroupForm, setShowGroupForm] = useState<boolean>(false);
    const [channelForm, setChannelForm] = useState<CreateOfficialChannelRequest>({
        name: '', description: '', category: 'STUDENTS_IRAN', displayMode: 'SPECIAL'
    });
    const [groupForm, setGroupForm] = useState<CreateOfficialGroupRequest>({
        name: '', description: '', category: 'STUDENTS_IRAN', hideMembers: false, displayMode: 'SPECIAL'
    });
    const [adminSearch, setAdminSearch] = useState<string>('');
    const [adminSearchResults, setAdminSearchResults] = useState<AdminUser[]>([]);
    const [expandedItem, setExpandedItem] = useState<string | null>(null);
    const [allUsers, setAllUsers] = useState<AdminUser[]>([]);
    const [editingChannelId, setEditingChannelId] = useState<string | null>(null);
    const [editingGroupId, setEditingGroupId] = useState<string | null>(null);
    const [usersLoaded, setUsersLoaded] = useState<boolean>(false);

    // Admin selection during creation
    const [selectedChannelAdmins, setSelectedChannelAdmins] = useState<AdminUser[]>([]);
    const [selectedGroupAdmins, setSelectedGroupAdmins] = useState<AdminUser[]>([]);
    const [createAdminSearch, setCreateAdminSearch] = useState<string>('');
    const [createAdminResults, setCreateAdminResults] = useState<AdminUser[]>([]);

    // Targeting state
    const [isChannelPublic, setIsChannelPublic] = useState<boolean>(true);
    const [isGroupPublic, setIsGroupPublic] = useState<boolean>(true);

    // Reference data for targeting dropdowns
    const [provinces, setProvinces] = useState<string[]>([]);
    const [cities, setCities] = useState<string[]>([]);
    const [universities, setUniversities] = useState<University[]>([]);
    const [fieldsOfStudy, setFieldsOfStudy] = useState<FieldOfStudy[]>([]);
    const [educationLevels, setEducationLevels] = useState<EducationLevel[]>([]);
    const [refDataLoaded, setRefDataLoaded] = useState<boolean>(false);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async (): Promise<void> => {
        setLoading(true);
        try {
            const [channelsRes, groupsRes] = await Promise.all([
                adminApi.getOfficialChannels(),
                adminApi.getOfficialGroups()
            ]);
            setChannels((channelsRes.data.data || []).reverse());
            setGroups((groupsRes.data.data || []).reverse());
        } catch (err) {
            console.error('Error loading data:', err);
        }
        setLoading(false);
    };

    // Load reference data for targeting dropdowns
    const loadRefData = useCallback(async (): Promise<void> => {
        if (refDataLoaded) return;
        try {
            const [provRes, uniRes, fosRes, elRes] = await Promise.all([
                adminApi.getProvinces('ایران'),
                adminApi.getUniversities(0, 9999),
                adminApi.getFieldsOfStudy(),
                adminApi.getEducationLevels()
            ]);
            setProvinces(provRes.data.data || []);
            setUniversities(uniRes.data.data?.content || []);
            setFieldsOfStudy(fosRes.data.data || []);
            setEducationLevels(elRes.data.data || []);
            setRefDataLoaded(true);
        } catch (err) {
            console.error('Error loading reference data:', err);
        }
    }, [refDataLoaded]);

    // Load cities when province changes
    const loadCities = async (province: string): Promise<void> => {
        if (!province) { setCities([]); return; }
        try {
            const res = await adminApi.getCities(province);
            setCities(res.data.data || []);
        } catch (err) {
            console.error('Error loading cities:', err);
        }
    };

    const loadUsers = useCallback(async (): Promise<void> => {
        if (usersLoaded) return;
        try {
            const res = await adminApi.getUsers(0, 9999);
            const rawUsers: any[] = res.data.data?.content || [];
            const users: AdminUser[] = rawUsers.map((u: any) => ({
                id: u.id,
                username: u.username,
                displayName: u.displayName,
                phoneNumber: u.phoneNumber,
                avatarUrl: u.avatarUrl
            }));
            setAllUsers(users);
            setUsersLoaded(true);
        } catch (err) {
            console.error('Error loading users:', err);
        }
    }, [usersLoaded]);

    const handleSearchUsers = (query: string): void => {
        setAdminSearch(query);
        if (query.length < 2) {
            setAdminSearchResults([]);
            return;
        }
        const filtered: AdminUser[] = allUsers.filter(u =>
            u.displayName.toLowerCase().includes(query.toLowerCase()) ||
            u.username.toLowerCase().includes(query.toLowerCase()) ||
            (u.phoneNumber && u.phoneNumber.includes(query))
        );
        setAdminSearchResults(filtered.slice(0, 10));
    };

    const handleCreateChannel = async (): Promise<void> => {
        if (!channelForm.name.trim()) return;
        try {
            const payload: CreateOfficialChannelRequest = {
                ...channelForm,
                adminIds: selectedChannelAdmins.map(a => a.id)
            };
            if (editingChannelId) {
                await adminApi.updateOfficialChannel(editingChannelId, payload);
            } else {
                await adminApi.createOfficialChannel(payload);
            }
            setShowChannelForm(false);
            setChannelForm({ name: '', description: '', category: 'STUDENTS_IRAN', displayMode: 'SPECIAL' });
            setSelectedChannelAdmins([]);
            setCreateAdminSearch('');
            setCreateAdminResults([]);
            setEditingChannelId(null);
            loadData();
        } catch (err) {
            console.error('Error saving channel:', err);
        }
    };

    const handleEditChannel = (channel: OfficialChannel): void => {
        setChannelForm({
            name: channel.name,
            description: channel.description || '',
            category: channel.category as OfficialChannelCategory,
            displayMode: channel.displayMode || 'SPECIAL',
            avatarUrl: channel.avatarUrl,
            targetFieldOfStudy: channel.targetFieldOfStudy,
            targetEducationLevel: channel.targetEducationLevel,
            targetProvince: channel.targetProvince,
            targetCity: channel.targetCity,
            targetUniversity: channel.targetUniversity,
        });
        setIsChannelPublic(!channel.targetProvince && !channel.targetCity && !channel.targetUniversity && !channel.targetFieldOfStudy && !channel.targetEducationLevel);
        setEditingChannelId(channel.id);
        setShowChannelForm(true);
    };

    const handleDeleteChannel = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این کانال مطمئن هستید؟')) return;
        try {
            await adminApi.deleteOfficialChannel(id);
            loadData();
        } catch (err) {
            console.error('Error deleting channel:', err);
        }
    };

    const handleAddChannelAdmin = async (channelId: string, userId: string): Promise<void> => {
        try {
            await adminApi.addChannelAdmin(channelId, userId);
            setAdminSearch('');
            setAdminSearchResults([]);
            loadData();
        } catch (err) {
            console.error('Error adding admin:', err);
        }
    };

    const handleRemoveChannelAdmin = async (channelId: string, userId: string): Promise<void> => {
        try {
            await adminApi.removeChannelAdmin(channelId, userId);
            loadData();
        } catch (err) {
            console.error('Error removing admin:', err);
        }
    };

    const handleCreateGroup = async (): Promise<void> => {
        if (!groupForm.name.trim()) return;
        try {
            const payload: CreateOfficialGroupRequest = {
                ...groupForm,
                adminIds: selectedGroupAdmins.map(a => a.id)
            };
            if (editingGroupId) {
                await adminApi.updateOfficialGroup(editingGroupId, payload);
            } else {
                await adminApi.createOfficialGroup(payload);
            }
            setShowGroupForm(false);
            setGroupForm({ name: '', description: '', category: 'STUDENTS_IRAN', hideMembers: false, displayMode: 'SPECIAL' });
            setSelectedGroupAdmins([]);
            setCreateAdminSearch('');
            setCreateAdminResults([]);
            setEditingGroupId(null);
            loadData();
        } catch (err) {
            console.error('Error saving group:', err);
        }
    };

    const handleEditGroup = (group: OfficialGroup): void => {
        setGroupForm({
            name: group.name,
            description: group.description || '',
            category: group.category as OfficialGroupCategory,
            hideMembers: group.hideMembers || false,
            displayMode: group.displayMode || 'SPECIAL',
            avatarUrl: group.avatarUrl,
            targetFieldOfStudy: group.targetFieldOfStudy,
            targetEducationLevel: group.targetEducationLevel,
            targetProvince: group.targetProvince,
            targetCity: group.targetCity,
            targetUniversity: group.targetUniversity,
        });
        setIsGroupPublic(!group.targetProvince && !group.targetCity && !group.targetUniversity && !group.targetFieldOfStudy && !group.targetEducationLevel);
        setEditingGroupId(group.id);
        setShowGroupForm(true);
    };

    const handleDeleteGroup = async (id: string): Promise<void> => {
        if (!confirm('آیا از حذف این گروه مطمئن هستید؟')) return;
        try {
            await adminApi.deleteOfficialGroup(id);
            loadData();
        } catch (err) {
            console.error('Error deleting group:', err);
        }
    };

    const handleAddGroupAdmin = async (groupId: string, userId: string): Promise<void> => {
        try {
            await adminApi.addGroupAdmin(groupId, userId);
            setAdminSearch('');
            setAdminSearchResults([]);
            loadData();
        } catch (err) {
            console.error('Error adding admin:', err);
        }
    };

    const handleRemoveGroupAdmin = async (groupId: string, userId: string): Promise<void> => {
        try {
            await adminApi.removeGroupAdmin(groupId, userId);
            loadData();
        } catch (err) {
            console.error('Error removing admin:', err);
        }
    };

    const toggleExpandItem = (id: string): void => {
        if (expandedItem === id) {
            setExpandedItem(null);
        } else {
            setExpandedItem(id);
            loadUsers();
        }
        setAdminSearch('');
        setAdminSearchResults([]);
    };

    const handleCreateAdminSearch = (query: string): void => {
        setCreateAdminSearch(query);
        if (!usersLoaded) loadUsers();
        if (query.length < 2) {
            setCreateAdminResults([]);
            return;
        }
        const filtered: AdminUser[] = allUsers.filter(u =>
            u.displayName.toLowerCase().includes(query.toLowerCase()) ||
            u.username.toLowerCase().includes(query.toLowerCase()) ||
            (u.phoneNumber && u.phoneNumber.includes(query))
        );
        setCreateAdminResults(filtered.slice(0, 10));
    };

    const renderCreateAdminPicker = (
        selectedAdmins: AdminUser[],
        onAdd: (user: AdminUser) => void,
        onRemove: (userId: string) => void
    ) => (
        <div className="mb-4 p-4 rounded-xl bg-emerald-500/5 border border-emerald-500/20 space-y-3">
            <h4 className="text-xs font-black text-emerald-400 uppercase tracking-widest flex items-center gap-2">
                <Shield size={14} /> انتخاب ادمین‌ها (اختیاری)
            </h4>
            {/* Selected Admins */}
            <div className="flex flex-wrap gap-2">
                {selectedAdmins.map((admin) => (
                    <div key={admin.id} className="flex items-center gap-2 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 px-3 py-1.5 rounded-lg text-xs font-bold">
                        <span>{admin.displayName}</span>
                        <span className="text-emerald-500/50">@{admin.username}</span>
                        <button
                            onClick={() => onRemove(admin.id)}
                            className="text-rose-400 hover:text-rose-300 transition-colors"
                        >
                            <UserMinus size={12} />
                        </button>
                    </div>
                ))}
                {selectedAdmins.length === 0 && (
                    <span className="text-xs text-slate-600">هنوز ادمینی انتخاب نشده</span>
                )}
            </div>
            {/* Search */}
            <div className="relative">
                <div className="flex items-center gap-2">
                    <Search size={14} className="text-slate-600" />
                    <input
                        type="text"
                        value={createAdminSearch}
                        onChange={(e) => handleCreateAdminSearch(e.target.value)}
                        placeholder="جستجوی کاربر برای افزودن ادمین..."
                        className="flex-1 glass bg-white/5 border-white/5 p-3 rounded-xl text-white text-xs placeholder:text-slate-600 outline-none focus:ring-2 focus:ring-emerald-500"
                    />
                </div>
                {createAdminResults.length > 0 && (
                    <div className="absolute top-full right-0 left-0 glass bg-admin-bg/95 backdrop-blur-xl border border-white/10 rounded-xl mt-2 z-20 max-h-48 overflow-y-auto">
                        {createAdminResults
                            .filter(u => !selectedAdmins.some(a => a.id === u.id))
                            .map((user) => (
                                <button
                                    key={user.id}
                                    onClick={() => { onAdd(user); setCreateAdminSearch(''); setCreateAdminResults([]); }}
                                    className="w-full flex items-center gap-3 p-3 text-right hover:bg-white/5 transition-colors border-b border-white/5 last:border-b-0"
                                >
                                    <UserPlus size={14} className="text-emerald-400" />
                                    <span className="text-sm font-bold text-white">{user.displayName}</span>
                                    <span className="text-xs text-slate-500">@{user.username}</span>
                                </button>
                            ))}
                    </div>
                )}
            </div>
        </div>
    );

    const renderAdminSection = (
        itemId: string,
        admins: AdminUser[],
        onAddAdmin: (userId: string) => void,
        onRemoveAdmin: (userId: string) => void
    ) => (
        <div className="mt-4 pt-4 border-t border-white/5">
            <h4 className="text-xs font-black text-slate-500 uppercase tracking-widest mb-3 flex items-center gap-2">
                <Shield size={14} className="text-indigo-400" />
                مدیریت ادمین‌ها
            </h4>
            {/* Current Admins */}
            <div className="flex flex-wrap gap-2 mb-4">
                {admins.map((admin) => (
                    <div key={admin.id} className="flex items-center gap-2 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 px-3 py-1.5 rounded-lg text-xs font-bold">
                        <span>{admin.displayName}</span>
                        <span className="text-emerald-500/50">@{admin.username}</span>
                        <button
                            onClick={() => onRemoveAdmin(admin.id)}
                            className="text-rose-400 hover:text-rose-300 transition-colors"
                        >
                            <UserMinus size={12} />
                        </button>
                    </div>
                ))}
                {admins.length === 0 && (
                    <span className="text-xs text-slate-600">بدون ادمین</span>
                )}
            </div>
            {/* Search & Add Admin */}
            <div className="relative">
                <div className="flex items-center gap-2">
                    <Search size={14} className="text-slate-600" />
                    <input
                        type="text"
                        value={expandedItem === itemId ? adminSearch : ''}
                        onChange={(e) => handleSearchUsers(e.target.value)}
                        placeholder="جستجوی کاربر برای افزودن ادمین..."
                        className="flex-1 glass bg-white/5 border-white/5 p-3 rounded-xl text-white text-xs placeholder:text-slate-600 outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                </div>
                {adminSearchResults.length > 0 && expandedItem === itemId && (
                    <div className="absolute top-full right-0 left-0 glass bg-admin-bg/95 backdrop-blur-xl border border-white/10 rounded-xl mt-2 z-20 max-h-48 overflow-y-auto">
                        {adminSearchResults.map((user) => (
                            <button
                                key={user.id}
                                onClick={() => onAddAdmin(user.id)}
                                className="w-full flex items-center gap-3 p-3 text-right hover:bg-white/5 transition-colors border-b border-white/5 last:border-b-0"
                            >
                                <UserPlus size={14} className="text-emerald-400" />
                                <span className="text-sm font-bold text-white">{user.displayName}</span>
                                <span className="text-xs text-slate-500">@{user.username}</span>
                            </button>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );

    const renderMultiSelect = (
        label: string,
        options: { value: string, label: string }[],
        value: string | undefined,
        onChange: (val: string | undefined) => void,
        disabled = false,
        disabledTooltip?: string
    ) => {
        const selectedList = value ? value.split(',').filter(Boolean) : [];
        
        return (
            <div className="space-y-1">
                <label className="text-xs font-bold text-slate-500">{label}</label>
                {selectedList.length > 0 && (
                    <div className="flex flex-wrap gap-1 mb-2">
                        {selectedList.map(item => {
                            const opt = options.find(o => o.value === item);
                            return (
                                <span key={item} className="bg-indigo-500/20 text-indigo-300 text-[10px] px-2 py-1 rounded-md flex items-center gap-1">
                                    {opt ? opt.label : item}
                                    <button type="button" onClick={() => {
                                        const newList = selectedList.filter(i => i !== item);
                                        onChange(newList.length > 0 ? newList.join(',') : undefined);
                                    }} className="hover:text-white"><X size={10} /></button>
                                </span>
                            );
                        })}
                    </div>
                )}
                <select
                    value=""
                    onChange={(e) => {
                        const val = e.target.value;
                        if (!val) return;
                        if (!selectedList.includes(val)) {
                            onChange([...selectedList, val].join(','));
                        }
                    }}
                    className="w-full glass bg-white/5 border-white/5 p-3 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none text-sm"
                    disabled={disabled}
                    title={disabled ? disabledTooltip : ''}
                >
                    <option value="" className="bg-slate-800">انتخاب کنید...</option>
                    {options.filter(o => !selectedList.includes(o.value)).map(o => (
                        <option key={o.value} value={o.value} className="bg-slate-800">{o.label}</option>
                    ))}
                </select>
            </div>
        );
    };

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-black text-white">کانال‌ها و گروه‌های رسمی</h1>
                <p className="text-slate-400 mt-2">مدیریت کانال‌ها و گروه‌های رسمی اپلیکیشن</p>
            </div>

            {/* Tabs */}
            <div className="flex gap-3">
                <button
                    onClick={() => setActiveSection('channels')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeSection === 'channels'
                        ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Megaphone size={18} />
                    کانال‌های رسمی ({channels.length})
                </button>
                <button
                    onClick={() => setActiveSection('groups')}
                    className={`flex items-center gap-3 px-6 py-3 rounded-2xl font-bold text-sm transition-all duration-300 ${activeSection === 'groups'
                        ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
                        : 'glass text-slate-400 hover:text-white'
                        }`}
                >
                    <Users size={18} />
                    گروه‌های رسمی ({groups.length})
                </button>
            </div>

            {loading && (
                <div className="flex justify-center py-20">
                    <Loader2 size={32} className="text-indigo-400 animate-spin" />
                </div>
            )}

            {/* ═══ CHANNELS ═══ */}
            {activeSection === 'channels' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <Megaphone size={20} className="text-violet-400" />
                            کانال‌های رسمی
                        </h2>
                        <button
                            onClick={() => setShowChannelForm(true)}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            ایجاد کانال
                        </button>
                    </div>

                    {showChannelForm && (
                        <div className="glass p-6 rounded-2xl border-emerald-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-emerald-400 mb-4">{editingChannelId ? '✏️ ویرایش کانال رسمی' : 'ایجاد کانال رسمی جدید'}</h3>
                            <div className="grid grid-cols-1 gap-4 mb-4">
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام کانال *</label>
                                    <input
                                        type="text"
                                        value={channelForm.name}
                                        onChange={(e) => setChannelForm({ ...channelForm, name: e.target.value })}
                                        placeholder="مثال: کانال رسمی دانشجویان ایران زمین"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                            </div>
                            <div className="mb-4 space-y-2">
                                <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">توضیحات</label>
                                <textarea
                                    value={channelForm.description || ''}
                                    onChange={(e) => setChannelForm({ ...channelForm, description: e.target.value })}
                                    placeholder="توضیح مختصر درباره کانال"
                                    rows={2}
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none placeholder:text-slate-600 resize-y"
                                />
                            </div>
                            <div className="mb-4 space-y-2">
                                <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">حالت نمایش</label>
                                <select
                                    value={channelForm.displayMode}
                                    onChange={(e) => setChannelForm({ ...channelForm, displayMode: e.target.value })}
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                >
                                    <option value="SPECIAL" style={{ color: '#000' }}>⭐ ویژه (بخش ویژه)</option>
                                    <option value="TAB" style={{ color: '#000' }}>📋 تب کانال‌ها</option>
                                    <option value="SUPPORT" style={{ color: '#000' }}>📞 ارتباط با پشتیبان</option>
                                </select>
                            </div>
                            {/* عمومی / غیر عمومی toggle */}
                            <div className="mb-4">
                                <label className="flex items-center gap-3 cursor-pointer text-sm text-slate-300 select-none">
                                    <input
                                        type="checkbox"
                                        checked={isChannelPublic}
                                        onChange={(e) => {
                                            setIsChannelPublic(e.target.checked);
                                            if (!e.target.checked) {
                                                loadRefData();
                                            } else {
                                                setChannelForm({ ...channelForm, targetFieldOfStudy: undefined, targetEducationLevel: undefined, targetProvince: undefined, targetCity: undefined, targetUniversity: undefined });
                                            }
                                        }}
                                        className="w-5 h-5 rounded accent-indigo-500"
                                    />
                                    <Globe size={16} className={isChannelPublic ? 'text-emerald-400' : 'text-slate-500'} />
                                    {isChannelPublic ? 'عمومی (همه می‌بینند)' : 'غیر عمومی (فیلتر مخاطبین)'}
                                </label>
                            </div>
                            {/* Targeting fields (shown when non-public) */}
                            {!isChannelPublic && (
                                <div className="mb-4 p-4 rounded-xl bg-indigo-500/5 border border-indigo-500/20 space-y-4">
                                    <p className="text-xs text-slate-400 flex items-center gap-2">
                                        <MapPin size={14} className="text-indigo-400" />
                                        فیلترهای مخاطبین — همه فیلدها اختیاری هستند و مستقل از هم عمل می‌کنند. هرچه کمتر پر کنید، عمومی‌تر خواهد بود
                                    </p>
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        {renderMultiSelect(
                                            "استان",
                                            provinces.map(p => ({ value: p, label: p })),
                                            channelForm.targetProvince,
                                            (val) => {
                                                setChannelForm({ ...channelForm, targetProvince: val, targetCity: undefined });
                                                if (val) {
                                                    const arr = val.split(',');
                                                    loadCities(arr[arr.length - 1]);
                                                } else {
                                                    loadCities("");
                                                }
                                            }
                                        )}
                                        {renderMultiSelect(
                                            "شهر",
                                            cities.map(c => ({ value: c, label: c })),
                                            channelForm.targetCity,
                                            (val) => setChannelForm({ ...channelForm, targetCity: val }),
                                            !channelForm.targetProvince,
                                            "ابتدا استان را انتخاب کنید"
                                        )}
                                        {renderMultiSelect(
                                            "دانشگاه",
                                            universities.map(u => ({ value: u.name, label: u.name })),
                                            channelForm.targetUniversity,
                                            (val) => setChannelForm({ ...channelForm, targetUniversity: val })
                                        )}
                                        {renderMultiSelect(
                                            "وزارت مربوطه",
                                            [
                                                "وزارت علوم", "وزارت بهداشت", "پیام نور", "دانشگاه آزاد", "فنی حرفه ای",
                                                "منابع طبیعی", "علمی کاربردی", "غیرانتفاعی", "ملی مهارت", "علوم قرآن و معارف",
                                                "هنر", "موسسه آموزش عالی", "فرهنگیان", "علوم پزشکی"
                                            ].map(m => ({ value: m, label: m })),
                                            channelForm.targetMinistry,
                                            (val) => setChannelForm({ ...channelForm, targetMinistry: val })
                                        )}
                                        {renderMultiSelect(
                                            "رشته تحصیلی",
                                            fieldsOfStudy.map(f => ({ value: f.name, label: f.name })),
                                            channelForm.targetFieldOfStudy,
                                            (val) => setChannelForm({ ...channelForm, targetFieldOfStudy: val })
                                        )}
                                        {renderMultiSelect(
                                            "مقطع تحصیلی",
                                            educationLevels.map(el => ({ value: el.name, label: el.name })),
                                            channelForm.targetEducationLevel,
                                            (val) => setChannelForm({ ...channelForm, targetEducationLevel: val })
                                        )}
                                    </div>
                                </div>
                            )}
                            {/* Admin Selection */}
                            {renderCreateAdminPicker(
                                selectedChannelAdmins,
                                (user: AdminUser) => setSelectedChannelAdmins([...selectedChannelAdmins, user]),
                                (userId: string) => setSelectedChannelAdmins(selectedChannelAdmins.filter(a => a.id !== userId))
                            )}
                            <div className="flex gap-3">
                                <button onClick={handleCreateChannel} className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-3 rounded-xl font-bold text-sm transition-all">
                                    <Check size={16} />
                                    {editingChannelId ? 'ذخیره تغییرات' : 'ایجاد'}
                                </button>
                                <button onClick={() => { setShowChannelForm(false); setEditingChannelId(null); setChannelForm({ name: '', description: '', category: 'STUDENTS_IRAN', displayMode: 'SPECIAL' }); }} className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-3 rounded-xl font-bold text-sm transition-all">
                                    <X size={16} />
                                    انصراف
                                </button>
                            </div>
                        </div>
                    )}

                    <div className="space-y-4">
                        {channels.map((channel) => (
                            <div key={channel.id} className={`glass p-5 rounded-2xl transition-all duration-300 group ${expandedItem === channel.id ? 'border-indigo-500/30' : 'border-white/5 hover:border-indigo-500/20'}`}>
                                <div
                                    className="flex justify-between items-center cursor-pointer"
                                    onClick={() => toggleExpandItem(channel.id)}
                                >
                                    <div className="flex items-center gap-4">
                                        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-violet-500/20 to-purple-600/20 flex items-center justify-center text-xl">📢</div>
                                        <div>
                                            <div className="text-sm font-bold text-white flex items-center gap-2">
                                                {channel.name}
                                                {channel.displayMode === 'SPECIAL' && (
                                                    <span className="text-[10px] bg-yellow-500/20 text-yellow-300 px-2 py-0.5 rounded-full font-bold">⭐ ویژه</span>
                                                )}
                                                {channel.displayMode === 'SUPPORT' && (
                                                    <span className="text-[10px] bg-green-500/20 text-green-300 px-2 py-0.5 rounded-full font-bold">📞 پشتیبان</span>
                                                )}
                                                {channel.displayMode === 'TAB' && (
                                                    <span className="text-[10px] bg-blue-500/20 text-blue-300 px-2 py-0.5 rounded-full font-bold">📋 تب</span>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-3 mt-1">
                                                <span className="text-[10px] bg-indigo-500/20 text-indigo-300 px-2 py-0.5 rounded-full font-bold">
                                                    {getCategoryLabel(channel.category, 'channel')}
                                                </span>
                                                <span className="text-xs text-slate-500">
                                                    {channel.subscriberCount} عضو • {channel.admins.length} ادمین
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <button
                                            onClick={(e) => { e.stopPropagation(); handleEditChannel(channel); }}
                                            className="p-2 bg-indigo-500/10 text-indigo-400 hover:bg-indigo-500/20 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                                        >
                                            <Pencil size={16} />
                                        </button>
                                        <button
                                            onClick={(e) => { e.stopPropagation(); handleDeleteChannel(channel.id); }}
                                            className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                        {expandedItem === channel.id ? <ChevronUp size={16} className="text-slate-500" /> : <ChevronDown size={16} className="text-slate-500" />}
                                    </div>
                                </div>
                                {expandedItem === channel.id && renderAdminSection(
                                    channel.id,
                                    channel.admins,
                                    (userId) => handleAddChannelAdmin(channel.id, userId),
                                    (userId) => handleRemoveChannelAdmin(channel.id, userId)
                                )}
                            </div>
                        ))}
                        {channels.length === 0 && (
                            <div className="glass p-12 rounded-2xl text-center text-slate-600 text-sm">
                                هنوز کانال رسمی ایجاد نشده است
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* ═══ GROUPS ═══ */}
            {activeSection === 'groups' && !loading && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center">
                        <h2 className="text-lg font-bold text-white flex items-center gap-3">
                            <Users size={20} className="text-emerald-400" />
                            گروه‌های رسمی
                        </h2>
                        <button
                            onClick={() => setShowGroupForm(true)}
                            className="flex items-center gap-3 bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95"
                        >
                            <Plus size={16} />
                            ایجاد گروه
                        </button>
                    </div>

                    {showGroupForm && (
                        <div className="glass p-6 rounded-2xl border-emerald-500/20 animate-in fade-in slide-in-from-top-4 duration-300">
                            <h3 className="text-sm font-black text-emerald-400 mb-4">{editingGroupId ? '✏️ ویرایش گروه رسمی' : 'ایجاد گروه رسمی جدید'}</h3>
                            <div className="grid grid-cols-1 gap-4 mb-4">
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">نام گروه *</label>
                                    <input
                                        type="text"
                                        value={groupForm.name}
                                        onChange={(e) => setGroupForm({ ...groupForm, name: e.target.value })}
                                        placeholder="مثال: گروه رسمی دانشجویان ایران زمین"
                                        className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none placeholder:text-slate-600"
                                    />
                                </div>
                            </div>
                            <div className="mb-4 space-y-2">
                                <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">توضیحات</label>
                                <textarea
                                    value={groupForm.description || ''}
                                    onChange={(e) => setGroupForm({ ...groupForm, description: e.target.value })}
                                    placeholder="توضیح مختصر درباره گروه"
                                    rows={2}
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none placeholder:text-slate-600 resize-y"
                                />
                            </div>
                            <div className="mb-4">
                                <label className="flex items-center gap-3 cursor-pointer text-sm text-slate-300 select-none">
                                    <input
                                        type="checkbox"
                                        checked={groupForm.hideMembers}
                                        onChange={(e) => setGroupForm({ ...groupForm, hideMembers: e.target.checked })}
                                        className="w-5 h-5 rounded accent-indigo-500"
                                    />
                                    {groupForm.hideMembers ? <EyeOff size={16} className="text-amber-400" /> : <Eye size={16} className="text-slate-500" />}
                                    مخفی‌سازی لیست اعضا
                                </label>
                            </div>
                            <div className="mb-4 space-y-2">
                                <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">حالت نمایش</label>
                                <select
                                    value={groupForm.displayMode}
                                    onChange={(e) => setGroupForm({ ...groupForm, displayMode: e.target.value })}
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                >
                                    <option value="SPECIAL" style={{ color: '#000' }}>⭐ ویژه (بخش ویژه)</option>
                                    <option value="TAB" style={{ color: '#000' }}>📋 تب گروه‌ها</option>
                                    <option value="SUPPORT" style={{ color: '#000' }}>📞 ارتباط با پشتیبان</option>
                                </select>
                            </div>
                            {/* عمومی / غیر عمومی toggle */}
                            <div className="mb-4">
                                <label className="flex items-center gap-3 cursor-pointer text-sm text-slate-300 select-none">
                                    <input
                                        type="checkbox"
                                        checked={isGroupPublic}
                                        onChange={(e) => {
                                            setIsGroupPublic(e.target.checked);
                                            if (!e.target.checked) {
                                                loadRefData();
                                            } else {
                                                setGroupForm({ ...groupForm, targetFieldOfStudy: undefined, targetEducationLevel: undefined, targetProvince: undefined, targetCity: undefined, targetUniversity: undefined });
                                            }
                                        }}
                                        className="w-5 h-5 rounded accent-indigo-500"
                                    />
                                    <Globe size={16} className={isGroupPublic ? 'text-emerald-400' : 'text-slate-500'} />
                                    {isGroupPublic ? 'عمومی (همه می‌بینند)' : 'غیر عمومی (فیلتر مخاطبین)'}
                                </label>
                            </div>
                            {/* Targeting fields (shown when non-public) */}
                            {!isGroupPublic && (
                                <div className="mb-4 p-4 rounded-xl bg-indigo-500/5 border border-indigo-500/20 space-y-4">
                                    <p className="text-xs text-slate-400 flex items-center gap-2">
                                        <MapPin size={14} className="text-indigo-400" />
                                        فیلترهای مخاطبین — همه فیلدها اختیاری هستند و مستقل از هم عمل می‌کنند. هرچه کمتر پر کنید، عمومی‌تر خواهد بود
                                    </p>
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        {renderMultiSelect(
                                            "استان",
                                            provinces.map(p => ({ value: p, label: p })),
                                            groupForm.targetProvince,
                                            (val) => {
                                                setGroupForm({ ...groupForm, targetProvince: val, targetCity: undefined });
                                                if (val) {
                                                    const arr = val.split(',');
                                                    loadCities(arr[arr.length - 1]);
                                                } else {
                                                    loadCities("");
                                                }
                                            }
                                        )}
                                        {renderMultiSelect(
                                            "شهر",
                                            cities.map(c => ({ value: c, label: c })),
                                            groupForm.targetCity,
                                            (val) => setGroupForm({ ...groupForm, targetCity: val }),
                                            !groupForm.targetProvince,
                                            "ابتدا استان را انتخاب کنید"
                                        )}
                                        {renderMultiSelect(
                                            "دانشگاه",
                                            universities.map(u => ({ value: u.name, label: u.name })),
                                            groupForm.targetUniversity,
                                            (val) => setGroupForm({ ...groupForm, targetUniversity: val })
                                        )}
                                        {renderMultiSelect(
                                            "وزارت مربوطه",
                                            [
                                                "وزارت علوم", "وزارت بهداشت", "پیام نور", "دانشگاه آزاد", "فنی حرفه ای",
                                                "منابع طبیعی", "علمی کاربردی", "غیرانتفاعی", "ملی مهارت", "علوم قرآن و معارف",
                                                "هنر", "موسسه آموزش عالی", "فرهنگیان", "علوم پزشکی"
                                            ].map(m => ({ value: m, label: m })),
                                            groupForm.targetMinistry,
                                            (val) => setGroupForm({ ...groupForm, targetMinistry: val })
                                        )}
                                        {renderMultiSelect(
                                            "رشته تحصیلی",
                                            fieldsOfStudy.map(f => ({ value: f.name, label: f.name })),
                                            groupForm.targetFieldOfStudy,
                                            (val) => setGroupForm({ ...groupForm, targetFieldOfStudy: val })
                                        )}
                                        {renderMultiSelect(
                                            "مقطع تحصیلی",
                                            educationLevels.map(el => ({ value: el.name, label: el.name })),
                                            groupForm.targetEducationLevel,
                                            (val) => setGroupForm({ ...groupForm, targetEducationLevel: val })
                                        )}
                                    </div>
                                </div>
                            )}
                            {/* Admin Selection */}
                            {renderCreateAdminPicker(
                                selectedGroupAdmins,
                                (user: AdminUser) => setSelectedGroupAdmins([...selectedGroupAdmins, user]),
                                (userId: string) => setSelectedGroupAdmins(selectedGroupAdmins.filter(a => a.id !== userId))
                            )}
                            <div className="flex gap-3">
                                <button onClick={handleCreateGroup} className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-3 rounded-xl font-bold text-sm transition-all">
                                    <Check size={16} />
                                    {editingGroupId ? 'ذخیره تغییرات' : 'ایجاد'}
                                </button>
                                <button onClick={() => { setShowGroupForm(false); setEditingGroupId(null); setGroupForm({ name: '', description: '', category: 'STUDENTS_IRAN', hideMembers: false, displayMode: 'SPECIAL' }); }} className="flex items-center gap-2 bg-white/5 hover:bg-white/10 text-slate-400 px-5 py-3 rounded-xl font-bold text-sm transition-all">
                                    <X size={16} />
                                    انصراف
                                </button>
                            </div>
                        </div>
                    )}

                    <div className="space-y-4">
                        {groups.map((group) => (
                            <div key={group.id} className={`glass p-5 rounded-2xl transition-all duration-300 group ${expandedItem === group.id ? 'border-indigo-500/30' : 'border-white/5 hover:border-emerald-500/20'}`}>
                                <div
                                    className="flex justify-between items-center cursor-pointer"
                                    onClick={() => toggleExpandItem(group.id)}
                                >
                                    <div className="flex items-center gap-4">
                                        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-500/20 to-green-600/20 flex items-center justify-center text-xl">👥</div>
                                        <div>
                                            <div className="text-sm font-bold text-white flex items-center gap-2">
                                                {group.name}
                                                {group.displayMode === 'SPECIAL' && (
                                                    <span className="text-[10px] bg-yellow-500/20 text-yellow-300 px-2 py-0.5 rounded-full font-bold">⭐ ویژه</span>
                                                )}
                                                {group.displayMode === 'SUPPORT' && (
                                                    <span className="text-[10px] bg-green-500/20 text-green-300 px-2 py-0.5 rounded-full font-bold">📞 پشتیبان</span>
                                                )}
                                                {group.displayMode === 'TAB' && (
                                                    <span className="text-[10px] bg-blue-500/20 text-blue-300 px-2 py-0.5 rounded-full font-bold">📋 تب</span>
                                                )}
                                                {group.hideMembers && (
                                                    <span className="text-[10px] bg-amber-500/20 text-amber-300 px-2 py-0.5 rounded-full font-bold flex items-center gap-1">
                                                        <EyeOff size={10} /> اعضا مخفی
                                                    </span>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-3 mt-1">
                                                <span className="text-[10px] bg-emerald-500/20 text-emerald-300 px-2 py-0.5 rounded-full font-bold">
                                                    {getCategoryLabel(group.category, 'group')}
                                                </span>
                                                <span className="text-xs text-slate-500">
                                                    {group.memberCount} عضو • {group.admins.length} ادمین
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <button
                                            onClick={(e) => { e.stopPropagation(); handleEditGroup(group); }}
                                            className="p-2 bg-indigo-500/10 text-indigo-400 hover:bg-indigo-500/20 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                                        >
                                            <Pencil size={16} />
                                        </button>
                                        <button
                                            onClick={(e) => { e.stopPropagation(); handleDeleteGroup(group.id); }}
                                            className="p-2 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                        {expandedItem === group.id ? <ChevronUp size={16} className="text-slate-500" /> : <ChevronDown size={16} className="text-slate-500" />}
                                    </div>
                                </div>
                                {expandedItem === group.id && renderAdminSection(
                                    group.id,
                                    group.admins,
                                    (userId) => handleAddGroupAdmin(group.id, userId),
                                    (userId) => handleRemoveGroupAdmin(group.id, userId)
                                )}
                            </div>
                        ))}
                        {groups.length === 0 && (
                            <div className="glass p-12 rounded-2xl text-center text-slate-600 text-sm">
                                هنوز گروه رسمی ایجاد نشده است
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default OfficialChannelsGroups;
