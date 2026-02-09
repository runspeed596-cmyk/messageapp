import { useState, useEffect, useRef } from 'react';
import { adminApi } from '../api/adminApi';
import type { EntertainmentMovie, EntertainmentMusic, EntertainmentRiddle, RiddleOption } from '../api/adminApi';
import { Film, Music, Brain, Plus, Trash2, Check, X, Upload, Loader2, Play, AlertCircle } from 'lucide-react';

type TabType = 'cinema' | 'music' | 'riddle';

const Entertainment = () => {
    const [activeTab, setActiveTab] = useState<TabType>('cinema');
    const [isAdding, setIsAdding] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    // Data lists
    const [movies, setMovies] = useState<EntertainmentMovie[]>([]);
    const [musicList, setMusicList] = useState<EntertainmentMusic[]>([]);
    const [riddles, setRiddles] = useState<EntertainmentRiddle[]>([]);

    // File refs
    const videoRef = useRef<HTMLInputElement>(null);
    const audioRef = useRef<HTMLInputElement>(null);

    // Video form
    const [videoFile, setVideoFile] = useState<File | null>(null);
    const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
    const [newMovie, setNewMovie] = useState<Partial<EntertainmentMovie>>({
        title: '', description: '', duration: '', releaseDate: '', isActive: true
    });

    // Music form
    const [audioFile, setAudioFile] = useState<File | null>(null);
    const [coverFile, setCoverFile] = useState<File | null>(null);
    const [newMusic, setNewMusic] = useState<Partial<EntertainmentMusic>>({
        title: '', artist: '', duration: '', isActive: true
    });

    // Riddle form
    const [optionCount, setOptionCount] = useState(4);
    const [newRiddle, setNewRiddle] = useState<Partial<EntertainmentRiddle>>({
        title: '', description: '', question: '', reward: '', type: 'RIDDLE',
        isMultipleChoice: true, options: [], correctAnswerIndex: 0, isActive: true
    });
    const [riddleOptions, setRiddleOptions] = useState<string[]>(['', '', '', '']);

    const tabs = [
        { id: 'cinema' as TabType, label: 'سینما کلاسوره', icon: Film, color: 'rose' },
        { id: 'music' as TabType, label: 'نواخانه', icon: Music, color: 'indigo' },
        { id: 'riddle' as TabType, label: 'باشگاه هوش', icon: Brain, color: 'purple' },
    ];

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [moviesRes, musicRes, riddlesRes] = await Promise.all([
                adminApi.getMovies(),
                adminApi.getMusic(),
                adminApi.getRiddles()
            ]);
            if (moviesRes.data.success) setMovies(moviesRes.data.data);
            if (musicRes.data.success) setMusicList(musicRes.data.data);
            if (riddlesRes.data.success) setRiddles(riddlesRes.data.data);
        } catch (error) {
            console.error('Error fetching entertainment data:', error);
        }
    };

    // === Video Handlers ===
    const handleSaveVideo = async () => {
        if (!videoFile || !newMovie.title) {
            alert('لطفاً فایل ویدیو و عنوان را وارد کنید');
            return;
        }
        setIsLoading(true);
        try {
            const videoUrl = await adminApi.uploadVideo(videoFile);
            let thumbnailUrl = '';
            if (thumbnailFile) {
                thumbnailUrl = await adminApi.uploadImage(thumbnailFile);
            }
            await adminApi.saveMovie({
                ...newMovie,
                videoUrl,
                thumbnailUrl,
                isActive: true
            } as EntertainmentMovie);
            fetchData();
            resetVideoForm();
            setIsAdding(false);
        } catch (error) {
            alert('خطا در آپلود ویدیو');
        } finally {
            setIsLoading(false);
        }
    };

    const resetVideoForm = () => {
        setVideoFile(null);
        setThumbnailFile(null);
        setNewMovie({ title: '', description: '', duration: '', releaseDate: '', isActive: true });
    };

    // === Music Handlers ===
    const handleSaveMusic = async () => {
        if (!audioFile || !newMusic.title) {
            alert('لطفاً فایل موسیقی و عنوان را وارد کنید');
            return;
        }
        setIsLoading(true);
        try {
            const audioUrl = await adminApi.uploadAudio(audioFile);
            let coverUrl = '';
            if (coverFile) {
                coverUrl = await adminApi.uploadImage(coverFile);
            }
            await adminApi.saveMusic({
                ...newMusic,
                audioUrl,
                coverUrl,
                isActive: true
            } as EntertainmentMusic);
            fetchData();
            resetMusicForm();
            setIsAdding(false);
        } catch (error) {
            alert('خطا در آپلود موسیقی');
        } finally {
            setIsLoading(false);
        }
    };

    const resetMusicForm = () => {
        setAudioFile(null);
        setCoverFile(null);
        setNewMusic({ title: '', artist: '', duration: '', isActive: true });
    };

    // === Riddle Handlers ===
    const handleOptionCountChange = (count: number) => {
        setOptionCount(count);
        const newOptions = [...riddleOptions];
        while (newOptions.length < count) newOptions.push('');
        while (newOptions.length > count) newOptions.pop();
        setRiddleOptions(newOptions);
    };

    const handleSaveRiddle = async () => {
        if (!newRiddle.question || !newRiddle.title) {
            alert('لطفاً عنوان و سوال معما را وارد کنید');
            return;
        }
        if (riddleOptions.some(o => !o.trim())) {
            alert('لطفاً تمام گزینه‌ها را پر کنید');
            return;
        }
        setIsLoading(true);
        try {
            const options: RiddleOption[] = riddleOptions.map((text, i) => ({
                text,
                displayOrder: i
            }));
            await adminApi.saveRiddle({
                ...newRiddle,
                options,
                isMultipleChoice: true,
                isActive: true
            } as EntertainmentRiddle);
            fetchData();
            resetRiddleForm();
            setIsAdding(false);
        } catch (error) {
            alert('خطا در ذخیره معما');
        } finally {
            setIsLoading(false);
        }
    };

    const resetRiddleForm = () => {
        setOptionCount(4);
        setRiddleOptions(['', '', '', '']);
        setNewRiddle({
            title: '', description: '', question: '', reward: '', type: 'RIDDLE',
            isMultipleChoice: true, options: [], correctAnswerIndex: 0, isActive: true
        });
    };

    // === Delete Handlers ===
    const handleDeleteMovie = async (id: string) => {
        if (window.confirm('آیا از حذف این ویدیو اطمینان دارید؟')) {
            await adminApi.deleteMovie(id);
            fetchData();
        }
    };

    const handleDeleteMusic = async (id: string) => {
        if (window.confirm('آیا از حذف این موسیقی اطمینان دارید؟')) {
            await adminApi.deleteMusic(id);
            fetchData();
        }
    };

    const handleDeleteRiddle = async (id: string) => {
        if (window.confirm('آیا از حذف این معما اطمینان دارید؟')) {
            await adminApi.deleteRiddle(id);
            fetchData();
        }
    };

    const renderAddForm = () => {
        if (!isAdding) return null;

        if (activeTab === 'cinema') {
            return (
                <div className="glass p-8 rounded-[2rem] border-rose-500/20 shadow-2xl animate-in fade-in">
                    <h3 className="text-xl font-black text-white mb-6 flex items-center gap-3">
                        <Film className="text-rose-400" /> افزودن ویدیو جدید
                    </h3>
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        <div className="space-y-4">
                            <label className="text-xs font-black text-slate-500 uppercase">آپلود فایل ویدیو</label>
                            <div
                                onClick={() => videoRef.current?.click()}
                                className={`aspect-video rounded-2xl border-2 border-dashed cursor-pointer flex items-center justify-center
                                    ${videoFile ? 'border-rose-500/50 bg-rose-500/10' : 'border-white/10 hover:border-rose-500/30'}`}
                            >
                                {videoFile ? (
                                    <div className="text-center">
                                        <Play size={48} className="mx-auto text-rose-400 mb-2" />
                                        <p className="text-white font-bold">{videoFile.name}</p>
                                    </div>
                                ) : (
                                    <div className="text-center">
                                        <Upload size={48} className="mx-auto text-slate-500 mb-2" />
                                        <p className="text-slate-400">کلیک کنید برای آپلود ویدیو</p>
                                        <p className="text-xs text-slate-600">MP4, WebM - حداکثر 500MB</p>
                                    </div>
                                )}
                            </div>
                            <input ref={videoRef} type="file" accept="video/*" className="hidden"
                                onChange={e => setVideoFile(e.target.files?.[0] || null)} />
                        </div>
                        <div className="space-y-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 uppercase">عنوان ویدیو</label>
                                <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-rose-500"
                                    placeholder="مثال: مستند علمی" value={newMovie.title}
                                    onChange={e => setNewMovie({ ...newMovie, title: e.target.value })} />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 uppercase">توضیحات</label>
                                <textarea className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none resize-none h-20 focus:ring-2 focus:ring-rose-500"
                                    placeholder="توضیحات ویدیو..." value={newMovie.description}
                                    onChange={e => setNewMovie({ ...newMovie, description: e.target.value })} />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <label className="text-xs font-black text-slate-500 uppercase">مدت زمان</label>
                                    <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-rose-500"
                                        placeholder="01:30:00" value={newMovie.duration}
                                        onChange={e => setNewMovie({ ...newMovie, duration: e.target.value })} />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-xs font-black text-slate-500 uppercase">تاریخ انتشار</label>
                                    <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-rose-500"
                                        placeholder="1404/01/01" value={newMovie.releaseDate}
                                        onChange={e => setNewMovie({ ...newMovie, releaseDate: e.target.value })} />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="flex gap-4 mt-8">
                        <button onClick={handleSaveVideo} disabled={isLoading}
                            className={`flex-1 py-4 rounded-2xl font-black flex items-center justify-center gap-3
                                ${isLoading ? 'bg-slate-600' : 'bg-rose-600 hover:bg-rose-500'} text-white`}>
                            {isLoading ? <><Loader2 className="animate-spin" /> در حال آپلود...</> : <><Check /> ذخیره ویدیو</>}
                        </button>
                        <button onClick={() => { setIsAdding(false); resetVideoForm(); }}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black">
                            <X className="inline mr-2" /> انصراف
                        </button>
                    </div>
                </div>
            );
        }

        if (activeTab === 'music') {
            return (
                <div className="glass p-8 rounded-[2rem] border-indigo-500/20 shadow-2xl animate-in fade-in">
                    <h3 className="text-xl font-black text-white mb-6 flex items-center gap-3">
                        <Music className="text-indigo-400" /> افزودن موسیقی جدید
                    </h3>
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        <div className="space-y-4">
                            <label className="text-xs font-black text-slate-500 uppercase">آپلود فایل صوتی</label>
                            <div
                                onClick={() => audioRef.current?.click()}
                                className={`h-40 rounded-2xl border-2 border-dashed cursor-pointer flex items-center justify-center
                                    ${audioFile ? 'border-indigo-500/50 bg-indigo-500/10' : 'border-white/10 hover:border-indigo-500/30'}`}
                            >
                                {audioFile ? (
                                    <div className="text-center">
                                        <Music size={48} className="mx-auto text-indigo-400 mb-2" />
                                        <p className="text-white font-bold">{audioFile.name}</p>
                                    </div>
                                ) : (
                                    <div className="text-center">
                                        <Upload size={48} className="mx-auto text-slate-500 mb-2" />
                                        <p className="text-slate-400">آپلود فایل صوتی</p>
                                        <p className="text-xs text-slate-600">MP3, WAV, AAC</p>
                                    </div>
                                )}
                            </div>
                            <input ref={audioRef} type="file" accept="audio/*" className="hidden"
                                onChange={e => setAudioFile(e.target.files?.[0] || null)} />
                        </div>
                        <div className="space-y-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 uppercase">عنوان موسیقی</label>
                                <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-indigo-500"
                                    placeholder="عنوان آهنگ" value={newMusic.title}
                                    onChange={e => setNewMusic({ ...newMusic, title: e.target.value })} />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 uppercase">نام هنرمند</label>
                                <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-indigo-500"
                                    placeholder="نام خواننده/نوازنده" value={newMusic.artist}
                                    onChange={e => setNewMusic({ ...newMusic, artist: e.target.value })} />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 uppercase">مدت زمان</label>
                                <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-indigo-500"
                                    placeholder="03:45" value={newMusic.duration}
                                    onChange={e => setNewMusic({ ...newMusic, duration: e.target.value })} />
                            </div>
                        </div>
                    </div>
                    <div className="flex gap-4 mt-8">
                        <button onClick={handleSaveMusic} disabled={isLoading}
                            className={`flex-1 py-4 rounded-2xl font-black flex items-center justify-center gap-3
                                ${isLoading ? 'bg-slate-600' : 'bg-indigo-600 hover:bg-indigo-500'} text-white`}>
                            {isLoading ? <><Loader2 className="animate-spin" /> در حال آپلود...</> : <><Check /> ذخیره موسیقی</>}
                        </button>
                        <button onClick={() => { setIsAdding(false); resetMusicForm(); }}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black">
                            <X className="inline mr-2" /> انصراف
                        </button>
                    </div>
                </div>
            );
        }

        if (activeTab === 'riddle') {
            return (
                <div className="glass p-8 rounded-[2rem] border-purple-500/20 shadow-2xl animate-in fade-in">
                    <h3 className="text-xl font-black text-white mb-6 flex items-center gap-3">
                        <Brain className="text-purple-400" /> افزودن معما جدید
                    </h3>
                    <div className="space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 uppercase">عنوان معما</label>
                                <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="مثال: چالش هوش ریاضی" value={newRiddle.title}
                                    onChange={e => setNewRiddle({ ...newRiddle, title: e.target.value })} />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-black text-slate-500 uppercase">جایزه (اختیاری)</label>
                                <input className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="مثال: ۱۰۰ سکه" value={newRiddle.reward}
                                    onChange={e => setNewRiddle({ ...newRiddle, reward: e.target.value })} />
                            </div>
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black text-slate-500 uppercase">متن سوال</label>
                            <textarea className="w-full glass bg-white/5 p-4 rounded-xl text-white outline-none resize-none h-24 focus:ring-2 focus:ring-purple-500"
                                placeholder="سوال معما را وارد کنید..." value={newRiddle.question}
                                onChange={e => setNewRiddle({ ...newRiddle, question: e.target.value })} />
                        </div>
                        <div className="space-y-4">
                            <div className="flex items-center gap-4">
                                <label className="text-xs font-black text-slate-500 uppercase">تعداد گزینه‌ها:</label>
                                <select className="glass bg-white/5 p-2 rounded-xl text-white outline-none"
                                    value={optionCount} onChange={e => handleOptionCountChange(parseInt(e.target.value))}>
                                    {[2, 3, 4, 5, 6, 7, 8, 9, 10].map(n => (
                                        <option key={n} value={n} className="bg-slate-900">{n} گزینه</option>
                                    ))}
                                </select>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                {riddleOptions.map((opt, i) => (
                                    <div key={i} className="flex items-center gap-2">
                                        <input type="radio" name="correct" checked={newRiddle.correctAnswerIndex === i}
                                            onChange={() => setNewRiddle({ ...newRiddle, correctAnswerIndex: i })}
                                            className="w-5 h-5 accent-purple-500" />
                                        <input className="flex-1 glass bg-white/5 p-3 rounded-xl text-white outline-none focus:ring-2 focus:ring-purple-500"
                                            placeholder={`گزینه ${i + 1}`} value={opt}
                                            onChange={e => {
                                                const newOpts = [...riddleOptions];
                                                newOpts[i] = e.target.value;
                                                setRiddleOptions(newOpts);
                                            }} />
                                    </div>
                                ))}
                            </div>
                            <p className="text-xs text-slate-500">گزینه صحیح را با کلیک روی دایره کنار آن انتخاب کنید</p>
                        </div>
                    </div>
                    <div className="flex gap-4 mt-8">
                        <button onClick={handleSaveRiddle} disabled={isLoading}
                            className={`flex-1 py-4 rounded-2xl font-black flex items-center justify-center gap-3
                                ${isLoading ? 'bg-slate-600' : 'bg-purple-600 hover:bg-purple-500'} text-white`}>
                            {isLoading ? <><Loader2 className="animate-spin" /> در حال ذخیره...</> : <><Check /> ذخیره معما</>}
                        </button>
                        <button onClick={() => { setIsAdding(false); resetRiddleForm(); }}
                            className="flex-1 bg-white/5 hover:bg-white/10 text-slate-400 py-4 rounded-2xl font-black">
                            <X className="inline mr-2" /> انصراف
                        </button>
                    </div>
                </div>
            );
        }

        return null;
    };

    const renderContent = () => {
        const items = activeTab === 'cinema' ? movies : activeTab === 'music' ? musicList : riddles;

        if (items.length === 0) {
            return (
                <div className="glass p-20 rounded-[2rem] text-center border-dashed border-white/10">
                    <AlertCircle size={40} className="text-slate-700 mx-auto mb-6" />
                    <h3 className="text-xl font-black text-white">هیچ محتوایی وجود ندارد</h3>
                    <p className="text-slate-500 mt-2">برای شروع، یک مورد جدید اضافه کنید.</p>
                </div>
            );
        }

        return (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {activeTab === 'cinema' && movies.map(movie => (
                    <div key={movie.id} className="glass rounded-[2rem] overflow-hidden group hover:border-rose-500/30 transition-all">
                        <div className="aspect-video bg-slate-800 relative">
                            {movie.thumbnailUrl ? (
                                <img src={movie.thumbnailUrl} alt={movie.title} className="w-full h-full object-cover" />
                            ) : (
                                <div className="w-full h-full flex items-center justify-center"><Film size={48} className="text-slate-600" /></div>
                            )}
                            <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                <Play size={48} className="text-white" />
                            </div>
                        </div>
                        <div className="p-4 flex justify-between items-center">
                            <div>
                                <h4 className="font-bold text-white">{movie.title}</h4>
                                <p className="text-xs text-slate-500">{movie.duration}</p>
                            </div>
                            <button onClick={() => handleDeleteMovie(movie.id!)} className="p-2 text-slate-400 hover:text-rose-400">
                                <Trash2 size={18} />
                            </button>
                        </div>
                    </div>
                ))}

                {activeTab === 'music' && musicList.map(music => (
                    <div key={music.id} className="glass rounded-[2rem] p-6 group hover:border-indigo-500/30 transition-all">
                        <div className="flex items-center gap-4">
                            <div className="w-16 h-16 rounded-xl bg-indigo-500/20 flex items-center justify-center">
                                <Music size={32} className="text-indigo-400" />
                            </div>
                            <div className="flex-1">
                                <h4 className="font-bold text-white">{music.title}</h4>
                                <p className="text-xs text-slate-500">{music.artist} • {music.duration}</p>
                            </div>
                            <button onClick={() => handleDeleteMusic(music.id!)} className="p-2 text-slate-400 hover:text-rose-400">
                                <Trash2 size={18} />
                            </button>
                        </div>
                    </div>
                ))}

                {activeTab === 'riddle' && riddles.map(riddle => (
                    <div key={riddle.id} className="glass rounded-[2rem] p-6 group hover:border-purple-500/30 transition-all">
                        <div className="flex justify-between items-start mb-4">
                            <div className="w-12 h-12 rounded-xl bg-purple-500/20 flex items-center justify-center">
                                <Brain size={24} className="text-purple-400" />
                            </div>
                            <button onClick={() => handleDeleteRiddle(riddle.id!)} className="p-2 text-slate-400 hover:text-rose-400">
                                <Trash2 size={18} />
                            </button>
                        </div>
                        <h4 className="font-bold text-white mb-2">{riddle.title}</h4>
                        <p className="text-xs text-slate-500 line-clamp-2">{riddle.question}</p>
                        {riddle.reward && (
                            <div className="mt-3 text-xs text-purple-400 font-bold">جایزه: {riddle.reward}</div>
                        )}
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className="space-y-8 rtl font-[Vazirmatn]">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-black text-white">کارخانه سرگرمی</h1>
                    <p className="text-slate-400 mt-2">مدیریت محتوای ویدیویی، صوتی و معماهای تعاملی</p>
                </div>
                <button onClick={() => setIsAdding(true)}
                    className="flex items-center gap-3 bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-3 rounded-2xl font-black shadow-lg transition-all active:scale-95 group">
                    <Plus size={20} className="group-hover:rotate-90 transition-transform" />
                    <span>افزودن محتوای جدید</span>
                </button>
            </div>

            <div className="flex gap-4">
                {tabs.map((tab) => (
                    <button key={tab.id} onClick={() => { setActiveTab(tab.id); setIsAdding(false); }}
                        className={`flex-1 flex items-center justify-center gap-4 p-6 rounded-[2rem] border transition-all duration-500
                            ${activeTab === tab.id
                                ? `bg-${tab.color}-500/10 border-${tab.color}-500/30 text-${tab.color}-400 scale-105 shadow-xl`
                                : 'glass border-white/5 text-slate-500 hover:border-white/20'}`}>
                        <tab.icon size={28} />
                        <span className="text-lg font-black">{tab.label}</span>
                    </button>
                ))}
            </div>

            {renderAddForm()}

            <div className="glass rounded-[2.5rem] p-8 min-h-[300px] border-white/5">
                <div className="flex justify-between items-center mb-8">
                    <h2 className="text-xl font-black text-white">
                        {activeTab === 'cinema' ? 'لیست ویدیوها' : activeTab === 'music' ? 'لیست موسیقی‌ها' : 'لیست معماها'}
                    </h2>
                    <span className="bg-white/5 text-slate-400 px-3 py-1 rounded-full text-xs font-bold">
                        {activeTab === 'cinema' ? movies.length : activeTab === 'music' ? musicList.length : riddles.length} مورد
                    </span>
                </div>
                {renderContent()}
            </div>
        </div>
    );
};

export default Entertainment;
