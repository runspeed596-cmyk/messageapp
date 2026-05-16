import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import type { University, Faculty, FieldOfStudy } from '../api/adminApi';
import Pagination from '../components/Pagination';
import {
    Plus, Check, X, GraduationCap, MapPin, Globe, BookOpen, Users as UsersIcon,
    Search, Trash2, Building2, FileText, Trophy, Loader2, Pencil, AlertTriangle,
    Settings, Award, Newspaper, UsersRound
} from 'lucide-react';
import { MapContainer, Marker, useMapEvents, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// ─── map.ir Configuration ───
const MAP_IR_API_KEY = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImM0MmU3MjNmNDA0M2YyYmUzMWQ2M2Y4OTZlNjg5NDkxZTFkYmNjMGJmMjQ4OGQ5Nzc3ZTdmNzkxZDQ2ZTU1NzQzOGQ0NmNlNTFhYTcxYmE2In0.eyJhdWQiOiI0MDQzNiIsImp0aSI6ImM0MmU3MjNmNDA0M2YyYmUzMWQ2M2Y4OTZlNjg5NDkxZTFkYmNjMGJmMjQ4OGQ5Nzc3ZTdmNzkxZDQ2ZTU1NzQzOGQ0NmNlNTFhYTcxYmE2IiwiaWF0IjoxNzc4MjQ5MTA5LCJuYmYiOjE3NzgyNDkxMDksImV4cCI6MTc4MDg0MTEwOSwic3ViIjoiIiwic2NvcGVzIjpbImJhc2ljIl19.lN70pBpP7W_16H_y5Y2uDzdjwpuDZpps5GDOt4JtyNFJmx0ddOKBk3jLe3dzm0TuxLTtNPe8hnSabOGnajppH1LsgptP6YYwkOs0Q0xTaBIX9GgAY6S1PA1cPIsNzpI-CkOHcoWa62foOM5eWhSvvQoDXe7DNvzAaa7gnyyh-P2ZJHwJ554W3stfq8y8K0h7hzLLY10eJH5m2o9rFAPti0QFgBhjxmSVVKaFQUO6J269-lye9SvKfCSTxa9NfiKmFhu7kMH0BR7TmjpLpI8LD41ZCi4m4Y6-IzbvwvfbBECmwrF1_oXcsEBxpmgF6KNyBrSdC7h_3GsKZuRWOcF8Rw';
const MAP_IR_SEARCH_URL = 'https://map.ir/search/v2/autocomplete';

// ─── Custom Leaflet TileLayer with x-api-key header for map.ir ───
const MapIrTileLayer = L.TileLayer.WMS.extend({
    createTile(coords: any, done: any): HTMLImageElement {
        const tile: HTMLImageElement = document.createElement('img');
        tile.alt = '';
        tile.setAttribute('role', 'presentation');
        const url: string = this.getTileUrl(coords);
        fetch(url, {
            headers: {
                'x-api-key': MAP_IR_API_KEY,
            },
        })
            .then((response: Response) => {
                if (!response.ok) throw new Error(`Tile fetch failed: ${response.status}`);
                return response.blob();
            })
            .then((blob: Blob) => {
                tile.src = URL.createObjectURL(blob);
                done(null, tile);
            })
            .catch((err: Error) => {
                console.error('Map.ir tile error:', err);
                done(err, tile);
            });
        return tile;
    },
});

// React component that adds the map.ir tile layer to the Leaflet map
const MapIrLayer = () => {
    const map = useMap();
    useEffect(() => {
        const layer = new (MapIrTileLayer as any)('https://map.ir/shiveh', {
            layers: 'Shiveh:Shiveh',
            format: 'image/png',
            transparent: true,
            attribution: '&copy; <a href="https://map.ir">Map.ir</a>',
        });
        layer.addTo(map);
        return () => {
            map.removeLayer(layer);
        };
    }, [map]);
    return null;
};

// Fix default marker icon issue with bundlers
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const MapClickHandler = ({ onMapClick }: { onMapClick: (lat: number, lng: number) => void }) => {
    useMapEvents({
        click(e) {
            onMapClick(e.latlng.lat, e.latlng.lng);
        },
    });
    return null;
};

const MapFlyTo = ({ lat, lng }: { lat: number; lng: number }) => {
    const map = useMap();
    useEffect(() => {
        map.flyTo([lat, lng], 14, { duration: 1.5 });
    }, [lat, lng, map]);
    return null;
};

// ─── Searchable Multi-Select Dropdown ───
interface MultiSelectOption {
    value: string;
    label: string;
    group?: string;
}

const SearchableMultiSelect = ({
    options,
    selected,
    onChange,
    placeholder,
    accentColor = 'indigo'
}: {
    options: MultiSelectOption[];
    selected: string[];
    onChange: (val: string[]) => void;
    placeholder: string;
    accentColor?: string;
}) => {
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [search, setSearch] = useState<string>('');
    const ref = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClick = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) setIsOpen(false);
        };
        document.addEventListener('mousedown', handleClick);
        return () => document.removeEventListener('mousedown', handleClick);
    }, []);

    const filtered: MultiSelectOption[] = options.filter(o =>
        o.label.toLowerCase().includes(search.toLowerCase()) && !selected.includes(o.value)
    );

    const grouped: Record<string, MultiSelectOption[]> = {};
    filtered.forEach(o => {
        const g: string = o.group || '';
        if (!grouped[g]) grouped[g] = [];
        grouped[g].push(o);
    });

    return (
        <div ref={ref} className="relative">
            <div
                className={`w-full glass bg-white/5 border-white/5 p-3 rounded-xl text-white cursor-pointer min-h-[52px] flex flex-wrap gap-2 items-center`}
                onClick={() => setIsOpen(!isOpen)}
            >
                {selected.length === 0 && <span className="text-slate-600 text-sm">{placeholder}</span>}
                {selected.map(s => {
                    const opt: MultiSelectOption | undefined = options.find(o => o.value === s);
                    return (
                        <span
                            key={s}
                            className={`bg-${accentColor}-500/20 text-${accentColor}-400 px-3 py-1 rounded-lg text-xs font-bold flex items-center gap-1`}
                        >
                            {opt?.label || s}
                            <X
                                size={12}
                                className="cursor-pointer hover:text-white"
                                onClick={(e) => { e.stopPropagation(); onChange(selected.filter(v => v !== s)); }}
                            />
                        </span>
                    );
                })}
            </div>
            {isOpen && (
                <div className="absolute z-50 top-full mt-2 left-0 right-0 glass bg-slate-900/95 backdrop-blur-xl rounded-xl border border-white/10 shadow-2xl max-h-60 overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200">
                    <div className="p-2 border-b border-white/5">
                        <input
                            type="text"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            placeholder="جستجو..."
                            className="w-full bg-white/5 p-2.5 rounded-lg text-white text-sm outline-none placeholder:text-slate-600"
                            autoFocus
                        />
                    </div>
                    <div className="overflow-y-auto max-h-48">
                        {Object.entries(grouped).map(([group, items]) => (
                            <div key={group}>
                                {group && (
                                    <div className="px-3 py-1.5 text-[10px] font-black text-slate-500 uppercase tracking-widest bg-white/3">
                                        {group}
                                    </div>
                                )}
                                {items.map(o => (
                                    <button
                                        key={o.value}
                                        className="w-full text-right px-3 py-2.5 text-sm text-slate-300 hover:bg-white/10 hover:text-white transition-colors"
                                        onClick={() => { onChange([...selected, o.value]); setSearch(''); }}
                                    >
                                        {o.label}
                                    </button>
                                ))}
                            </div>
                        ))}
                        {filtered.length === 0 && (
                            <div className="p-4 text-center text-slate-600 text-xs">موردی یافت نشد</div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

const Universities = () => {
    const navigate = useNavigate();
    const [unis, setUnis] = useState<University[]>([]);
    const [isAdding, setIsAdding] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [editingUniId, setEditingUniId] = useState<string | null>(null);
    const [error, setError] = useState<string>('');

    // Pagination state
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [totalElements, setTotalElements] = useState<number>(0);
    const PAGE_SIZE: number = 20;

    // Reference data
    const [refFaculties, setRefFaculties] = useState<Faculty[]>([]);
    const [refFields, setRefFields] = useState<FieldOfStudy[]>([]);

    // Location data
    const [countries] = useState<string[]>(['ایران']);
    const [provinces, setProvinces] = useState<string[]>([]);
    const [cities, setCities] = useState<string[]>([]);

    // Map search
    const [mapSearch, setMapSearch] = useState<string>('');
    const [mapSearchResults, setMapSearchResults] = useState<any[]>([]);
    const [mapFlyTarget, setMapFlyTarget] = useState<{ lat: number; lng: number } | null>(null);

    const [facultyAssignments, setFacultyAssignments] = useState<{ id: string; faculty: string; fields: string[] }[]>([]);

    const [newUni, setNewUni] = useState<University>({
        name: '',
        country: 'ایران',
        province: '',
        city: '',
        ministryName: 'وزارت علوم',
        type: 'دولتی',
        establishedYear: 1300,
        studentCount: 0,
        faculties: '',
        departments: '',
        iranRank: 0,
        worldRank: 0,
        articleCount: 0,
        journalCount: 0,
        facilities: '',
        latitude: 35.6892,
        longitude: 51.3890,
        imageUrl: '',
        websiteUrl: '',
        honors: '',
        professorCount: 0,
        professorNames: '',
        publicationCount: 0,
        studentOrgs: '',
        lastAdmissionCapacity: ''
    });

    useEffect(() => {
        fetchUnis();
        fetchProvinces('ایران');
        fetchReferenceData();
    }, []);

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => setError(''), 4000);
            return () => clearTimeout(timer);
        }
    }, [error]);

    const fetchUnis = async (page: number = 0) => {
        try {
            const response = await adminApi.getUniversities(page, PAGE_SIZE);
            if (response.data.success) {
                const paginated = response.data.data;
                setUnis(paginated.content);
                setTotalPages(paginated.totalPages);
                setTotalElements(paginated.totalElements);
                setCurrentPage(paginated.number);
            }
        } catch (err) {
            console.error('Error fetching universities:', err);
        }
    };

    const handlePageChange = (page: number): void => {
        fetchUnis(page);
    };

    const fetchReferenceData = async () => {
        try {
            const [facRes, fieldRes] = await Promise.all([
                adminApi.getFaculties(),
                adminApi.getFieldsOfStudy()
            ]);
            setRefFaculties(facRes.data.data || []);
            setRefFields(fieldRes.data.data || []);
        } catch (err) {
            console.error('Error fetching reference data:', err);
        }
    };

    const fetchProvinces = async (country: string) => {
        try {
            const response = await adminApi.getProvinces(country);
            if (response.data.success) {
                setProvinces(response.data.data);
            }
        } catch (err) {
            console.error('Error fetching provinces:', err);
        }
    };

    const fetchCities = async (province: string) => {
        try {
            const response = await adminApi.getCities(province);
            if (response.data.success) {
                setCities(response.data.data);
            }
        } catch (err) {
            console.error('Error fetching cities:', err);
        }
    };

    const handleProvinceChange = (province: string) => {
        setNewUni({ ...newUni, province, city: '' });
        fetchCities(province);
    };

    const handleMapSearch = async () => {
        if (!mapSearch.trim()) return;
        try {
            setMapSearchResults([]);
            const res = await fetch(
                `${MAP_IR_SEARCH_URL}?text=${encodeURIComponent(mapSearch)}&$filter=province eq تهران&$select=roads,poi`,
                {
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'x-api-key': MAP_IR_API_KEY
                    }
                }
            );
            if (!res.ok) {
                setError(`خطا در جستجو (${res.status})`);
                return;
            }
            const data = await res.json();
            const results: any[] = data.value || data.odata || data || [];
            if (results.length === 0) {
                setError('نتیجه‌ای یافت نشد. عبارت دیگری جستجو کنید.');
                return;
            }
            setMapSearchResults(results.slice(0, 5));
        } catch (err) {
            console.error('Map search error:', err);
            setError('خطا در ارتباط با سرویس نقشه. لطفاً دوباره تلاش کنید.');
        }
    };

    const handleSelectMapResult = (result: any) => {
        const geom = result.geom;
        let lat: number;
        let lng: number;
        if (geom && geom.coordinates) {
            lng = geom.coordinates[0];
            lat = geom.coordinates[1];
        } else if (result.lat && result.lon) {
            lat = parseFloat(result.lat);
            lng = parseFloat(result.lon);
        } else {
            return;
        }
        setNewUni({ ...newUni, latitude: parseFloat(lat.toFixed(6)), longitude: parseFloat(lng.toFixed(6)) });
        setMapFlyTarget({ lat, lng });
        setMapSearchResults([]);
        setMapSearch('');
    };

    const handleSave = async () => {
        if (!newUni.name || !newUni.province || !newUni.city) {
            setError('لطفاً نام دانشگاه، استان و شهر را وارد کنید');
            return;
        }
        setIsLoading(true);
        try {
            const uniToSave: University = {
                ...newUni,
                iranRank: Number(newUni.iranRank) || 0,
                worldRank: Number(newUni.worldRank) || 0,
                studentCount: Number(newUni.studentCount) || 0,
                articleCount: Number(newUni.articleCount) || 0,
                journalCount: Number(newUni.journalCount) || 0,
                establishedYear: Number(newUni.establishedYear) || 1300,
                professorCount: Number(newUni.professorCount) || 0,
                publicationCount: Number(newUni.publicationCount) || 0,
            };
            const res = await adminApi.saveUniversity(uniToSave);
            if (!res.data.success) {
                setError(res.data.message || 'خطا در ذخیره دانشگاه');
                return;
            }
            fetchUnis();
            setIsAdding(false);
            setEditingUniId(null);
            resetForm();
        } catch (err: any) {
            const msg: string = err?.response?.data?.message || 'خطا در ذخیره دانشگاه';
            setError(msg);
        } finally {
            setIsLoading(false);
        }
    };

    const handleEdit = (uni: University): void => {
        setNewUni({ ...uni });
        setEditingUniId(uni.id!);
        setIsAdding(true);
        if (uni.province) {
            fetchCities(uni.province);
        }

        const assignments: { id: string; faculty: string; fields: string[] }[] = [];
        if (uni.departments) {
            if (uni.departments.includes(':')) {
                uni.departments.split('|').forEach((part, i) => {
                    const [fac, flds] = part.split(':');
                    if (fac && flds) {
                        assignments.push({
                            id: Date.now().toString() + '-' + i,
                            faculty: fac.trim(),
                            fields: flds.split(',').map(s => s.trim()).filter(Boolean)
                        });
                    }
                });
            } else {
                 const faculties = uni.faculties ? uni.faculties.split(',').map(s=>s.trim()).filter(Boolean) : [];
                 if (faculties.length > 0) {
                     assignments.push({
                         id: Date.now().toString(),
                         faculty: faculties[0],
                         fields: uni.departments.split(',').map(s=>s.trim()).filter(Boolean)
                     });
                 }
            }
        }
        setFacultyAssignments(assignments);
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('آیا از حذف این دانشگاه اطمینان دارید؟')) {
            try {
                await adminApi.deleteUniversity(id);
                setUnis(unis.filter(u => u.id !== id));
            } catch (err) {
                setError('خطا در حذف دانشگاه');
            }
        }
    };

    const resetForm = () => {
        setNewUni({
            name: '',
            country: 'ایران',
            province: '',
            city: '',
            ministryName: 'وزارت علوم',
            type: 'دولتی',
            establishedYear: 1300,
            studentCount: 0,
            faculties: '',
            departments: '',
            iranRank: 0,
            worldRank: 0,
            articleCount: 0,
            journalCount: 0,
            facilities: '',
            latitude: 35.6892,
            longitude: 51.3890,
            imageUrl: '',
            websiteUrl: '',
            honors: '',
            professorCount: 0,
            professorNames: '',
            publicationCount: 0,
            studentOrgs: '',
            lastAdmissionCapacity: ''
        });
        setCities([]);
        setMapFlyTarget(null);
        setFacultyAssignments([]);
    };

    // ─── Build multi-select options from reference data ───

    const fieldOptions: MultiSelectOption[] = refFields
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map(f => ({ value: `${f.name} (${f.educationLevel})`, label: `${f.name} — ${f.educationLevel}`, group: f.educationLevel }));

    const updateFacultyAssignments = (newAssignments: typeof facultyAssignments) => {
        setFacultyAssignments(newAssignments);
        const faculties = newAssignments.map(a => a.faculty).filter(Boolean);
        const uniqueFaculties = Array.from(new Set(faculties));
        const deps = newAssignments
            .filter(a => a.faculty && a.fields.length > 0)
            .map(a => `${a.faculty}:${a.fields.join(',')}`)
            .join('|');
            
        setNewUni(prev => ({
            ...prev,
            faculties: uniqueFaculties.join(', '),
            departments: deps
        }));
    };

    const ministryOptions: string[] = [
        'وزارت علوم',
        'وزارت بهداشت',
        'پیام نور',
        'دانشگاه آزاد',
        'فنی حرفه ای',
        'منابع طبیعی',
        'علمی کاربردی',
        'غیرانتفاعی',
        'ملی مهارت',
        'علوم قرآن و معارف',
        'هنر',
        'موسسه آموزش عالی',
        'فرهنگیان',
        'علوم پزشکی'
    ];

    const typeOptions: string[] = ['دولتی', 'آزاد اسلامی', 'غیرانتفاعی', 'علوم پزشکی', 'پیام نور', 'فنی و حرفه‌ای', 'جامع علمی کاربردی'];

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            {/* Error Toast */}
            {error && (
                <div className="fixed top-6 left-1/2 -translate-x-1/2 z-[9999] bg-rose-600/90 backdrop-blur-xl text-white px-6 py-3 rounded-2xl shadow-2xl shadow-rose-500/30 flex items-center gap-3 animate-in fade-in slide-in-from-top-4 duration-300 border border-rose-400/30">
                    <AlertTriangle size={18} />
                    <span className="font-bold text-sm">{error}</span>
                </div>
            )}

            <div className="flex justify-between items-end flex-wrap gap-4">
                <div>
                    <h1 className="text-3xl font-black text-white">جهان علم — بخش اصلی</h1>
                    <p className="text-slate-400 mt-2">مدیریت دانشگاه‌ها و مراکز آموزش عالی در نقشه کره زمین</p>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={() => navigate('/world-of-science-settings')}
                        className="flex items-center gap-2 glass text-slate-400 hover:text-white hover:bg-white/10 px-5 py-3 rounded-2xl font-bold text-sm transition-all"
                    >
                        <Settings size={18} />
                        تنظیمات
                    </button>
                    <button
                        onClick={() => setIsAdding(true)}
                        className="flex items-center gap-3 bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-3 rounded-2xl font-black shadow-lg shadow-indigo-500/20 transition-all active:scale-95 group"
                    >
                        <Plus size={20} className="group-hover:rotate-90 transition-transform" />
                        <span>ثبت دانشگاه جدید</span>
                    </button>
                </div>
            </div>

            {isAdding && (
                <div className="glass p-8 rounded-[2rem] border-indigo-500/20 shadow-2xl animate-in fade-in slide-in-from-top-4 duration-500">
                    <div className="flex items-center gap-4 mb-8">
                        <div className="w-12 h-12 bg-white/10 rounded-2xl flex items-center justify-center text-indigo-400">
                            <GraduationCap size={24} />
                        </div>
                        <h2 className="text-xl font-black text-white">{editingUniId ? '✏️ ویرایش مرکز علمی' : 'ثبت مرکز علمی جدید'}</h2>
                    </div>

                    {/* Location Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <MapPin size={16} /> موقعیت جغرافیایی
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">کشور</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none cursor-pointer"
                                    value={newUni.country}
                                    onChange={e => setNewUni({ ...newUni, country: e.target.value })}
                                >
                                    {countries.map(c => <option key={c} className="bg-slate-900">{c}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">استان</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none cursor-pointer"
                                    value={newUni.province}
                                    onChange={e => handleProvinceChange(e.target.value)}
                                >
                                    <option value="" className="bg-slate-900">انتخاب استان...</option>
                                    {provinces.map(p => <option key={p} className="bg-slate-900">{p}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">شهر</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none cursor-pointer"
                                    value={newUni.city}
                                    onChange={e => setNewUni({ ...newUni, city: e.target.value })}
                                    disabled={!newUni.province}
                                >
                                    <option value="" className="bg-slate-900">انتخاب شهر...</option>
                                    {cities.map(c => <option key={c} className="bg-slate-900">{c}</option>)}
                                </select>
                            </div>
                        </div>

                        {/* Map search bar */}
                        <div className="mt-4 relative z-[9999]">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest mb-2 block">
                                🔍 جستجوی مکان روی نقشه
                            </label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={mapSearch}
                                    onChange={(e) => setMapSearch(e.target.value)}
                                    onKeyDown={(e) => { if (e.key === 'Enter') handleMapSearch(); }}
                                    placeholder="نام دانشگاه یا آدرس را تایپ کنید..."
                                    className="flex-1 glass bg-white/5 border-white/5 p-3 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none text-sm placeholder:text-slate-600"
                                />
                                <button
                                    onClick={handleMapSearch}
                                    className="bg-emerald-600 hover:bg-emerald-500 text-white px-5 rounded-xl font-bold text-sm transition-all flex items-center gap-2"
                                >
                                    <Search size={16} />
                                    جستجو
                                </button>
                            </div>
                            {mapSearchResults.length > 0 && (
                                <div className="absolute z-[10000] top-full mt-1 left-0 right-0 glass bg-slate-900/95 backdrop-blur-xl rounded-xl border border-white/10 shadow-2xl max-h-48 overflow-y-auto">
                                    {mapSearchResults.map((r: any, i: number) => (
                                        <button
                                            key={i}
                                            className="w-full text-right px-4 py-3 text-sm text-slate-300 hover:bg-white/10 hover:text-white transition-colors border-b border-white/5 last:border-0 flex items-center gap-2"
                                            onClick={() => handleSelectMapResult(r)}
                                        >
                                            <MapPin size={14} className="text-emerald-400 shrink-0" />
                                            <span className="truncate">{r.title || r.address || r.display_name || r.province}</span>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>

                        <div className="mt-4">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest mb-2 block">
                                📍 موقعیت دقیق روی نقشه (کلیک کنید)
                            </label>
                            <div className="rounded-2xl overflow-hidden border border-white/10" style={{ height: '350px' }}>
                                <MapContainer
                                    center={[newUni.latitude, newUni.longitude]}
                                    zoom={6}
                                    style={{ height: '100%', width: '100%' }}
                                    scrollWheelZoom={true}
                                >
                                    <MapIrLayer />
                                    <Marker position={[newUni.latitude, newUni.longitude]} />
                                    <MapClickHandler onMapClick={(lat, lng) => setNewUni({ ...newUni, latitude: parseFloat(lat.toFixed(6)), longitude: parseFloat(lng.toFixed(6)) })} />
                                    {mapFlyTarget && <MapFlyTo lat={mapFlyTarget.lat} lng={mapFlyTarget.lng} />}
                                </MapContainer>
                            </div>
                            <div className="flex gap-6 mt-3">
                                <span className="text-xs text-slate-500">
                                    عرض جغرافیایی: <strong className="text-indigo-400">{newUni.latitude}</strong>
                                </span>
                                <span className="text-xs text-slate-500">
                                    طول جغرافیایی: <strong className="text-indigo-400">{newUni.longitude}</strong>
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Basic Info Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <Building2 size={16} /> اطلاعات پایه
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            <div className="space-y-2 lg:col-span-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">نام کامل دانشگاه</label>
                                <input
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    placeholder="مثال: دانشگاه تهران"
                                    value={newUni.name}
                                    onChange={e => setNewUni({ ...newUni, name: e.target.value })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">سال تأسیس</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.establishedYear}
                                    onChange={e => setNewUni({ ...newUni, establishedYear: parseInt(e.target.value) })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">وزارت مربوطه</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none cursor-pointer"
                                    value={newUni.ministryName}
                                    onChange={e => setNewUni({ ...newUni, ministryName: e.target.value })}
                                >
                                    {ministryOptions.map(m => <option key={m} className="bg-slate-900">{m}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">نوع دانشگاه</label>
                                <select
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none cursor-pointer"
                                    value={newUni.type}
                                    onChange={e => setNewUni({ ...newUni, type: e.target.value })}
                                >
                                    {typeOptions.map(t => <option key={t} className="bg-slate-900">{t}</option>)}
                                </select>
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تعداد دانشجو</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.studentCount}
                                    onChange={e => setNewUni({ ...newUni, studentCount: parseInt(e.target.value) })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Academic Info Section — Dynamic Rows */}
                    <div className="mb-8 relative z-50">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="text-sm font-black text-indigo-400 flex items-center gap-2">
                                <BookOpen size={16} /> دانشکده‌ها و رشته‌های تحصیلی
                            </h3>
                            <button
                                onClick={() => {
                                    updateFacultyAssignments([...facultyAssignments, { id: Date.now().toString() + Math.random(), faculty: '', fields: [] }]);
                                }}
                                className="flex items-center gap-1 bg-indigo-500/20 text-indigo-400 hover:bg-indigo-500/30 px-3 py-1.5 rounded-lg text-xs font-bold transition-all"
                            >
                                <Plus size={14} />
                                افزودن دانشکده
                            </button>
                        </div>
                        
                        <div className="space-y-4 glass bg-white/5 border-white/5 p-4 rounded-xl pr-2 overflow-visible">
                            {facultyAssignments.length === 0 ? (
                                <div className="text-center text-slate-500 text-sm py-8">
                                    هنوز دانشکده‌ای اضافه نشده است. برای شروع روی «افزودن دانشکده» کلیک کنید.
                                </div>
                            ) : (
                                facultyAssignments.map((assignment, index) => (
                                    <div key={assignment.id} style={{ zIndex: 50 - index }} className="p-5 bg-slate-900/50 rounded-xl border border-white/5 relative group">
                                        <button
                                            onClick={() => {
                                                const newAssignments = [...facultyAssignments];
                                                newAssignments.splice(index, 1);
                                                updateFacultyAssignments(newAssignments);
                                            }}
                                            className="absolute top-3 left-3 text-slate-500 hover:text-rose-400 bg-slate-800 hover:bg-rose-500/10 p-1.5 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                                            title="حذف این دانشکده"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                        
                                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                                            <div className="space-y-2">
                                                <label className="text-xs font-black text-slate-500 uppercase tracking-widest block">انتخاب دانشکده</label>
                                                <select
                                                    className="w-full glass bg-slate-800 border-white/5 p-3.5 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none cursor-pointer text-sm"
                                                    value={assignment.faculty}
                                                    onChange={(e) => {
                                                        const newAssignments = [...facultyAssignments];
                                                        newAssignments[index].faculty = e.target.value;
                                                        updateFacultyAssignments(newAssignments);
                                                    }}
                                                >
                                                    <option value="" className="bg-slate-900">انتخاب کنید...</option>
                                                    {refFaculties.sort((a,b) => a.displayOrder - b.displayOrder).map(f => (
                                                        <option key={f.id} value={f.name} className="bg-slate-900">{f.name}</option>
                                                    ))}
                                                </select>
                                            </div>
                                            
                                            <div className="space-y-2 lg:col-span-2">
                                                <label className="text-xs font-black text-slate-500 uppercase tracking-widest block">رشته‌های تحصیلی</label>
                                                {assignment.faculty ? (
                                                    <SearchableMultiSelect
                                                         options={fieldOptions}
                                                         selected={assignment.fields}
                                                         onChange={(val) => {
                                                              const newAssignments = [...facultyAssignments];
                                                              newAssignments[index].fields = val;
                                                              updateFacultyAssignments(newAssignments);
                                                         }}
                                                         placeholder={`جستجو و افزودن رشته‌های ${assignment.faculty}...`}
                                                         accentColor="indigo"
                                                    />
                                                ) : (
                                                    <div className="w-full glass bg-slate-800/50 border-white/5 p-3.5 rounded-xl text-slate-500 text-sm flex items-center min-h-[52px]">
                                                        ابتدا یک دانشکده انتخاب کنید
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                        <p className="text-[10px] text-slate-600 mt-2">با کلیک روی «افزودن دانشکده»، می‌توانید دانشکده‌های مختلف را انتخاب کرده و برای هرکدام بی‌نهایت رشته تخصیص دهید.</p>
                    </div>

                    {/* Rankings Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <Trophy size={16} /> رتبه‌بندی و آمار
                        </h3>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">رتبه در ایران</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.iranRank || ''}
                                    onChange={e => setNewUni({ ...newUni, iranRank: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">رتبه در جهان</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.worldRank || ''}
                                    onChange={e => setNewUni({ ...newUni, worldRank: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تعداد مقالات</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.articleCount}
                                    onChange={e => setNewUni({ ...newUni, articleCount: parseInt(e.target.value) })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تعداد مجلات</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none"
                                    value={newUni.journalCount}
                                    onChange={e => setNewUni({ ...newUni, journalCount: parseInt(e.target.value) })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Extended Info Section — NEW */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-emerald-400 mb-4 flex items-center gap-2">
                            <Award size={16} /> اطلاعات تکمیلی
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">تعداد اساتید</label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none"
                                    value={newUni.professorCount || 0}
                                    onChange={e => setNewUni({ ...newUni, professorCount: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div className="space-y-2 lg:col-span-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">نام اساتید هر رشته</label>
                                <textarea
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none resize-none h-20"
                                    placeholder="مثال: دکتر احمدی (کامپیوتر)، دکتر محمدی (ریاضی)"
                                    value={newUni.professorNames || ''}
                                    onChange={e => setNewUni({ ...newUni, professorNames: e.target.value })}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest flex items-center gap-1">
                                    <Newspaper size={12} /> تعداد مجلات و نشریات
                                </label>
                                <input
                                    type="number"
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none"
                                    value={newUni.publicationCount || 0}
                                    onChange={e => setNewUni({ ...newUni, publicationCount: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div className="space-y-2 lg:col-span-2">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest flex items-center gap-1">
                                    <UsersRound size={12} /> انجمن‌ها و کانون‌های دانشجویی
                                </label>
                                <textarea
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none resize-none h-20"
                                    placeholder="مثال: انجمن علمی کامپیوتر، کانون فرهنگی، باشگاه ورزشی"
                                    value={newUni.studentOrgs || ''}
                                    onChange={e => setNewUni({ ...newUni, studentOrgs: e.target.value })}
                                />
                            </div>
                            <div className="space-y-2 md:col-span-2 lg:col-span-3">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">افتخارات</label>
                                <textarea
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none resize-none h-20"
                                    placeholder="افتخارات و دستاوردهای دانشگاه"
                                    value={newUni.honors || ''}
                                    onChange={e => setNewUni({ ...newUni, honors: e.target.value })}
                                />
                            </div>
                            <div className="space-y-2 md:col-span-2 lg:col-span-3">
                                <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">آخرین ظرفیت پذیرش هر رشته</label>
                                <textarea
                                    className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none resize-none h-20"
                                    placeholder="مثال: کامپیوتر ۱۲۰ نفر، پزشکی ۶۰ نفر"
                                    value={newUni.lastAdmissionCapacity || ''}
                                    onChange={e => setNewUni({ ...newUni, lastAdmissionCapacity: e.target.value })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Facilities Section */}
                    <div className="mb-8">
                        <h3 className="text-sm font-black text-indigo-400 mb-4 flex items-center gap-2">
                            <FileText size={16} /> امکانات
                        </h3>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 mr-2 uppercase tracking-widest">امکانات دانشگاه</label>
                            <textarea
                                className="w-full glass bg-white/5 border-white/5 p-4 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none resize-none h-24"
                                placeholder="مثال: کتابخانه مرکزی، خوابگاه، سالن ورزشی، آزمایشگاه‌های تخصصی"
                                value={newUni.facilities}
                                onChange={e => setNewUni({ ...newUni, facilities: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="flex gap-4 mt-10">
                        <button
                            onClick={handleSave}
                            disabled={isLoading}
                            className={`flex-1 text-white py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all
                                ${isLoading ? 'bg-slate-600 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-500'}`}
                        >
                            {isLoading ? (
                                <><Loader2 size={20} className="animate-spin" /> در حال ذخیره...</>
                            ) : (
                                <><Check size={20} /> ثبت نهایی اطلاعات</>
                            )}
                        </button>
                        <button
                            onClick={() => { setIsAdding(false); setEditingUniId(null); resetForm(); }}
                            disabled={isLoading}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black flex items-center justify-center gap-3 transition-all"
                        >
                            <X size={20} /> انصراف و بستن فرم
                        </button>
                    </div>
                </div>
            )}

            <div className="grid grid-cols-1 gap-4">
                {unis.map((uni) => (
                    <div key={uni.id} className="glass p-6 rounded-[1.5rem] border-white/5 hover:border-indigo-500/30 transition-all group">
                        <div className="flex justify-between items-start">
                            <div className="flex items-center gap-6">
                                <div className="w-16 h-16 bg-white/5 text-indigo-400 rounded-2xl flex items-center justify-center border border-white/10 group-hover:scale-110 transition-transform duration-500">
                                    <GraduationCap size={32} />
                                </div>
                                <div>
                                    <h3 className="font-black text-xl text-white">{uni.name}</h3>
                                    <div className="text-xs text-slate-500 flex flex-wrap gap-4 mt-2 font-bold">
                                        <span className="flex items-center gap-2 text-indigo-400">
                                            <MapPin size={12} /> {uni.city}, {uni.province}
                                        </span>
                                        <span className="flex items-center gap-2">
                                            <UsersIcon size={12} /> {uni.studentCount?.toLocaleString('fa-IR')} دانشجو
                                        </span>
                                        <span className="flex items-center gap-2 text-purple-400">
                                            <BookOpen size={12} /> {uni.type}
                                        </span>
                                        {uni.iranRank ? (
                                            <span className="flex items-center gap-2 text-emerald-400">
                                                <Trophy size={12} /> رتبه {uni.iranRank} در ایران
                                            </span>
                                        ) : null}
                                        {uni.professorCount ? (
                                            <span className="flex items-center gap-2 text-amber-400">
                                                <UsersRound size={12} /> {uni.professorCount} استاد
                                            </span>
                                        ) : null}
                                    </div>
                                    {uni.faculties && (
                                        <div className="text-xs text-slate-600 mt-2">
                                            دانشکده‌ها: {uni.faculties}
                                        </div>
                                    )}
                                    {uni.honors && (
                                        <div className="text-xs text-emerald-600 mt-1 flex items-center gap-1">
                                            <Award size={10} /> {uni.honors.substring(0, 100)}{uni.honors.length > 100 ? '...' : ''}
                                        </div>
                                    )}
                                </div>
                            </div>
                            <div className="flex gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
                                <button
                                    onClick={() => handleEdit(uni)}
                                    className="p-3 bg-white/5 text-slate-400 hover:text-indigo-400 hover:bg-indigo-500/10 rounded-xl transition-all"
                                >
                                    <Pencil size={20} />
                                </button>
                                <button className="p-3 bg-white/5 text-slate-400 hover:text-white hover:bg-white/10 rounded-xl transition-all">
                                    <Globe size={20} />
                                </button>
                                <button
                                    onClick={() => handleDelete(uni.id!)}
                                    className="p-3 bg-white/5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-xl transition-all"
                                >
                                    <Trash2 size={20} />
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
                {unis.length === 0 && (
                    <div className="glass p-20 rounded-[2rem] text-center border-dashed border-white/10">
                        <Search size={40} className="text-slate-700 mx-auto mb-6" />
                        <h3 className="text-xl font-black text-white">هیچ دانشگاهی ثبت نشده است</h3>
                        <p className="text-slate-500 mt-2">برای نمایش در کره زمین، حداقل یک دانشگاه اضافه کنید.</p>
                    </div>
                )}
            </div>

            {/* Pagination */}
            <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                totalElements={totalElements}
                pageSize={PAGE_SIZE}
                onPageChange={handlePageChange}
            />
        </div>
    );
};

export default Universities;
