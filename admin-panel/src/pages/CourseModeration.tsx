import { useState, useEffect } from 'react';
import { adminApi, getMediaUrl } from '../api/adminApi';
import type { CourseResponse } from '../api/adminApi';

const STATUS_LABELS: Record<string, string> = {
    PENDING: 'در انتظار تایید',
    APPROVED: 'تایید شده',
    REJECTED: 'رد شده',
    DRAFT: 'پیش‌نویس',
};

const STATUS_COLORS: Record<string, string> = {
    PENDING: '#f59e0b',
    APPROVED: '#10b981',
    REJECTED: '#ef4444',
    DRAFT: '#6b7280',
};

const s = {
    card: {
        background: 'rgba(30, 41, 59, 0.7)',
        borderRadius: 16,
        border: '1px solid rgba(255,255,255,0.1)',
        backdropFilter: 'blur(12px)',
    } as React.CSSProperties,
    infoBox: {
        background: 'rgba(15, 23, 42, 0.6)',
        padding: 14,
        borderRadius: 12,
        border: '1px solid rgba(255,255,255,0.06)',
    } as React.CSSProperties,
    label: {
        color: '#94a3b8',
        fontSize: 12,
        display: 'block' as const,
        marginBottom: 6,
        fontWeight: 600,
    } as React.CSSProperties,
    value: {
        fontWeight: 700,
        color: '#e2e8f0',
        fontSize: 14,
    } as React.CSSProperties,
};

export default function CourseModeration() {
    const [courses, setCourses] = useState<CourseResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedCourse, setSelectedCourse] = useState<CourseResponse | null>(null);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [adminNote, setAdminNote] = useState('');
    const [processing, setProcessing] = useState(false);
    const [organizerDetails, setOrganizerDetails] = useState<any>(null);

    const loadOrganizerDetails = async (id: string) => {
        try {
            const res = await adminApi.getUser(id);
            setOrganizerDetails(res.data?.data);
        } catch (e) {
            console.error('Failed to fetch user', e);
            alert('خطا در دریافت اطلاعات برگزارکننده');
        }
    };

    const fetchCourses = async () => {
        setLoading(true);
        try {
            const res = await adminApi.getPendingCourses(0, 50);
            const fetched = res.data?.data?.content || [];
            // Sort newest first
            fetched.sort((a: CourseResponse, b: CourseResponse) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
            setCourses(fetched);
        } catch (e) {
            console.error('Failed to fetch pending courses', e);
        }
        setLoading(false);
    };

    useEffect(() => { fetchCourses(); }, []);

    const handleApprove = async (course: CourseResponse) => {
        setProcessing(true);
        try {
            const adminId = localStorage.getItem('admin_id') || '';
            await adminApi.reviewCourse(course.id, adminId, 'APPROVED');
            alert('✅ دوره با موفقیت تایید شد');
            await fetchCourses();
            setSelectedCourse(null);
        } catch (e) {
            console.error(e);
            alert('❌ خطا در تایید دوره');
        }
        setProcessing(false);
    };

    const handleReject = async () => {
        if (!selectedCourse) return;
        setProcessing(true);
        try {
            const adminId = localStorage.getItem('admin_id') || '';
            await adminApi.reviewCourse(selectedCourse.id, adminId, 'REJECTED', adminNote);
            alert('❌ دوره رد شد');
            await fetchCourses();
            setShowRejectModal(false);
            setSelectedCourse(null);
            setAdminNote('');
        } catch (e) {
            console.error(e);
            alert('❌ خطا در رد دوره');
        }
        setProcessing(false);
    };

    const formatPrice = (rials: number): string => {
        if (rials === 0) return 'رایگان';
        return `${(rials / 10).toLocaleString('fa-IR')} تومان`;
    };

    return (
        <div style={{ direction: 'rtl', padding: 24 }}>
            <h2 style={{ marginBottom: 24, fontWeight: 800, fontSize: 24, color: '#f8fafc', display: 'flex', alignItems: 'center', gap: 12 }}>
                📚 مدیریت دوره‌ها
                <span style={{ fontSize: 13, fontWeight: 500, color: '#94a3b8', background: 'rgba(99,102,241,0.15)', padding: '4px 14px', borderRadius: 20 }}>
                    {courses.length} دوره در انتظار
                </span>
            </h2>

            {loading ? (
                <div style={{ textAlign: 'center', padding: 64, color: '#94a3b8' }}>
                    <div style={{ fontSize: 40, marginBottom: 12 }}>⏳</div>
                    در حال بارگذاری...
                </div>
            ) : courses.length === 0 ? (
                <div style={{ ...s.card, textAlign: 'center', padding: 64 }}>
                    <div style={{ fontSize: 48, marginBottom: 12 }}>✅</div>
                    <p style={{ fontSize: 18, fontWeight: 600, color: '#94a3b8' }}>هیچ دوره‌ای در انتظار تایید نیست</p>
                </div>
            ) : (
                <div style={{ display: 'grid', gap: 16 }}>
                    {courses.map(course => (
                        <div key={course.id} style={{
                            ...s.card,
                            padding: 20, display: 'flex', gap: 16, alignItems: 'flex-start',
                            cursor: 'pointer', transition: 'all 0.2s ease',
                        }}
                        onMouseEnter={e => { (e.currentTarget as HTMLDivElement).style.borderColor = 'rgba(99,102,241,0.4)'; }}
                        onMouseLeave={e => { (e.currentTarget as HTMLDivElement).style.borderColor = 'rgba(255,255,255,0.1)'; }}
                        onClick={() => setSelectedCourse(course)}>
                            {/* Poster */}
                            <div style={{ width: 90, height: 120, borderRadius: 12, overflow: 'hidden', flexShrink: 0, background: 'rgba(15,23,42,0.6)' }}>
                                {course.coverImageUrl ? (
                                    <img src={getMediaUrl(course.coverImageUrl)} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                ) : (
                                    <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 36, color: '#475569' }}>📖</div>
                                )}
                            </div>
                            {/* Info */}
                            <div style={{ flex: 1 }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                                    <h3 style={{ margin: 0, fontSize: 16, color: '#f1f5f9', fontWeight: 700 }}>{course.title}</h3>
                                    <span style={{ background: STATUS_COLORS[course.status] || '#6b7280', color: '#fff', padding: '3px 12px', borderRadius: 12, fontSize: 11, fontWeight: 700 }}>
                                        {STATUS_LABELS[course.status] || course.status}
                                    </span>
                                </div>
                                {course.slogan && <p style={{ margin: '4px 0', color: '#94a3b8', fontSize: 13 }}>{course.slogan}</p>}
                                <p style={{ margin: '6px 0', fontSize: 13, color: '#64748b' }}>
                                    برگزارکننده: <span style={{ color: '#818cf8' }}>{course.organizerName || 'نامشخص'}</span> &nbsp;|&nbsp; اساتید: {course.teachers.length} &nbsp;|&nbsp; قیمت: {formatPrice(course.priceRials)}
                                </p>
                                <p style={{ margin: '2px 0', fontSize: 12, color: '#475569' }}>
                                    ثبت: {new Date(course.createdAt).toLocaleDateString('fa-IR')} | ظرفیت: {course.enrollmentLimit || '∞'} | ثبت‌نامی: {course.enrolledCount}
                                </p>
                            </div>
                            {/* Actions */}
                            <div style={{ display: 'flex', gap: 8, flexShrink: 0 }} onClick={e => e.stopPropagation()}>
                                <button onClick={() => handleApprove(course)} disabled={processing}
                                    style={{ background: 'linear-gradient(135deg, #10b981, #059669)', color: '#fff', border: 'none', borderRadius: 10, padding: '10px 18px', cursor: 'pointer', fontWeight: 700, fontSize: 13, boxShadow: '0 4px 12px rgba(16,185,129,0.3)' }}>
                                    ✅ تایید
                                </button>
                                <button onClick={() => { setSelectedCourse(course); setShowRejectModal(true); }} disabled={processing}
                                    style={{ background: 'rgba(239,68,68,0.15)', color: '#ef4444', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 18px', cursor: 'pointer', fontWeight: 700, fontSize: 13 }}>
                                    ❌ رد
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Detail Panel */}
            {selectedCourse && !showRejectModal && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)',
                    display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000,
                }} onClick={() => setSelectedCourse(null)}>
                    <div style={{
                        ...s.card, maxWidth: 800, width: '92%', maxHeight: '88vh',
                        overflow: 'auto', padding: 32, direction: 'rtl',
                        boxShadow: '0 25px 50px rgba(0,0,0,0.5)',
                    }} onClick={e => e.stopPropagation()}>
                        {/* Header */}
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.08)', paddingBottom: 16, marginBottom: 24 }}>
                            <h2 style={{ margin: 0, fontSize: 22, fontWeight: 800, color: '#f8fafc' }}>📋 بررسی دوره: {selectedCourse.title}</h2>
                            <span style={{ background: STATUS_COLORS[selectedCourse.status] || '#6b7280', color: '#fff', padding: '6px 16px', borderRadius: 20, fontSize: 13, fontWeight: 'bold' }}>
                                {STATUS_LABELS[selectedCourse.status] || selectedCourse.status}
                            </span>
                        </div>
                        
                        {/* Poster + Info Grid */}
                        <div style={{ display: 'flex', gap: 24, marginBottom: 24, flexWrap: 'wrap' }}>
                            {selectedCourse.coverImageUrl ? (
                                <div style={{ flexShrink: 0, width: 220, height: 300, borderRadius: 16, overflow: 'hidden', boxShadow: '0 8px 24px rgba(0,0,0,0.4)' }}>
                                    <img src={getMediaUrl(selectedCourse.coverImageUrl)} alt="Poster" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                </div>
                            ) : (
                                <div style={{ flexShrink: 0, width: 220, height: 300, borderRadius: 16, background: 'rgba(15,23,42,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px dashed rgba(255,255,255,0.1)' }}>
                                    <span style={{ color: '#475569', fontSize: 14 }}>بدون پوستر</span>
                                </div>
                            )}
                            
                            <div style={{ flex: 1, minWidth: 300, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
                                <div style={s.infoBox}>
                                    <strong style={s.label}>رشته تحصیلی / سطح</strong>
                                    <span style={s.value}>{selectedCourse.fieldOfStudy || '-'} / {selectedCourse.educationLevel || '-'}</span>
                                </div>
                                <div style={s.infoBox}>
                                    <strong style={s.label}>هزینه ثبت‌نام</strong>
                                    <span style={{ ...s.value, color: '#f87171', fontSize: 15 }}>{formatPrice(selectedCourse.priceRials)}</span>
                                </div>
                                <div style={s.infoBox}>
                                    <strong style={s.label}>ظرفیت و نوع</strong>
                                    <span style={s.value}>{selectedCourse.enrollmentLimit || 'نامحدود'} | {selectedCourse.priceRials === 0 ? 'رایگان' : 'پولی'}</span>
                                </div>
                                <div style={s.infoBox}>
                                    <strong style={s.label}>برگزارکننده</strong>
                                    <span 
                                        style={{ ...s.value, color: '#818cf8', cursor: 'pointer', textDecoration: 'underline' }}
                                        onClick={() => loadOrganizerDetails(selectedCourse.organizerId)}
                                    >
                                        {selectedCourse.organizerName || '-'}
                                    </span>
                                </div>
                                <div style={{ ...s.infoBox, gridColumn: '1 / -1', background: 'rgba(99,102,241,0.08)', border: '1px solid rgba(99,102,241,0.15)' }}>
                                    <strong style={{ ...s.label, color: '#a5b4fc' }}>اساتید دوره</strong>
                                    <span style={{ ...s.value, color: '#c4b5fd' }}>{selectedCourse.teachers.map(t => t.displayName).join('، ') || 'ثبت نشده'}</span>
                                </div>
                                <div style={{ ...s.infoBox, gridColumn: '1 / -1' }}>
                                    <strong style={s.label}>شعار / چکیده</strong>
                                    <span style={{ ...s.value, color: '#cbd5e1' }}>{selectedCourse.slogan || '-'}</span>
                                </div>
                            </div>
                        </div>

                        {/* Description + Chapters */}
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 24 }}>
                            <div style={s.infoBox}>
                                <strong style={{ color: '#e2e8f0', fontSize: 16, display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12, fontWeight: 800 }}>📝 توضیحات کامل</strong>
                                <p style={{ margin: 0, lineHeight: 1.8, fontSize: 14, color: '#94a3b8', whiteSpace: 'pre-wrap' }}>{selectedCourse.description || '-'}</p>
                            </div>
                            
                            <div style={s.infoBox}>
                                <strong style={{ color: '#e2e8f0', fontSize: 16, display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12, fontWeight: 800 }}>📚 سرفصل‌ها ({selectedCourse.chapters.length})</strong>
                                {selectedCourse.chapters.length > 0 ? (
                                    <ul style={{ margin: 0, paddingLeft: 0, listStyle: 'none', display: 'flex', flexDirection: 'column', gap: 8 }}>
                                        {selectedCourse.chapters.map((c, i) => (
                                            <li key={i} style={{ background: 'rgba(15,23,42,0.5)', padding: '10px 14px', borderRadius: 10, border: '1px solid rgba(255,255,255,0.05)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                <span style={{ fontWeight: 600, color: '#e2e8f0', fontSize: 14 }}>{i+1}. {c.title}</span>
                                                <span style={{ fontSize: 12, color: '#64748b', background: 'rgba(99,102,241,0.1)', padding: '2px 10px', borderRadius: 12 }}>{c.durationText || 'نامشخص'}</span>
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p style={{ margin: 0, color: '#475569', fontSize: 14, textAlign: 'center', padding: 20 }}>سرفصلی ثبت نشده است</p>
                                )}
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div style={{ display: 'flex', gap: 14, marginTop: 32 }}>
                            {selectedCourse.status === 'PENDING' && (
                                <>
                                    <button onClick={() => handleApprove(selectedCourse)} disabled={processing}
                                        style={{ flex: 1, background: 'linear-gradient(135deg, #10b981, #059669)', color: '#fff', border: 'none', borderRadius: 14, padding: '14px 20px', cursor: 'pointer', fontWeight: 800, fontSize: 16, boxShadow: '0 4px 16px rgba(16,185,129,0.3)' }}>
                                        ✅ تایید انتشار دوره
                                    </button>
                                    <button onClick={() => setShowRejectModal(true)} disabled={processing}
                                        style={{ flex: 1, background: 'rgba(239,68,68,0.1)', color: '#ef4444', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 14, padding: '14px 20px', cursor: 'pointer', fontWeight: 800, fontSize: 16 }}>
                                        ❌ رد دوره (نیاز به اصلاح)
                                    </button>
                                </>
                            )}
                            <button onClick={() => setSelectedCourse(null)}
                                style={{ flex: 0.5, background: 'rgba(255,255,255,0.05)', color: '#94a3b8', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 14, padding: '14px 20px', cursor: 'pointer', fontWeight: 700, fontSize: 16 }}>
                                بستن
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Reject Modal */}
            {showRejectModal && selectedCourse && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.6)',
                    display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1001,
                    backdropFilter: 'blur(4px)',
                }} onClick={() => { setShowRejectModal(false); setAdminNote(''); }}>
                    <div style={{ ...s.card, maxWidth: 480, width: '90%', padding: 28, direction: 'rtl' }} onClick={e => e.stopPropagation()}>
                        <h3 style={{ marginTop: 0, color: '#ef4444', fontSize: 18, fontWeight: 800 }}>❌ رد دوره: {selectedCourse.title}</h3>
                        <p style={{ color: '#94a3b8', fontSize: 14 }}>لطفاً دلیل رد کردن دوره را برای برگزارکننده بنویسید:</p>
                        <textarea
                            value={adminNote} onChange={e => setAdminNote(e.target.value)}
                            placeholder="دلیل رد / نکات اصلاحی..."
                            style={{
                                width: '100%', minHeight: 100, borderRadius: 12,
                                border: '1px solid rgba(255,255,255,0.1)', padding: 14, fontSize: 14,
                                resize: 'vertical', direction: 'rtl',
                                background: 'rgba(15,23,42,0.8)', color: '#e2e8f0',
                                outline: 'none',
                            }}
                        />
                        <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
                            <button onClick={handleReject} disabled={processing || !adminNote.trim()}
                                style={{ flex: 1, background: '#ef4444', color: '#fff', border: 'none', borderRadius: 12, padding: 14, cursor: 'pointer', fontWeight: 700, fontSize: 15 }}>
                                {processing ? 'در حال ثبت...' : 'رد دوره'}
                            </button>
                            <button onClick={() => { setShowRejectModal(false); setAdminNote(''); }}
                                style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#94a3b8', borderRadius: 12, padding: '14px 18px', cursor: 'pointer', fontWeight: 600 }}>
                                انصراف
                            </button>
                        </div>
                    </div>
                </div>
            )}
            {/* Organizer Details Modal */}
            {organizerDetails && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.8)',
                    display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1100,
                }} onClick={() => setOrganizerDetails(null)}>
                    <div style={{
                        ...s.card, width: 400, padding: 24, direction: 'rtl'
                    }} onClick={e => e.stopPropagation()}>
                        <h3 style={{ margin: '0 0 16px 0', color: '#f8fafc' }}>اطلاعات برگزارکننده</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                            <div style={s.infoBox}>
                                <strong style={s.label}>نام و نام خانوادگی</strong>
                                <span style={s.value}>{organizerDetails.firstName || ''} {organizerDetails.lastName || ''}</span>
                            </div>
                            <div style={s.infoBox}>
                                <strong style={s.label}>کد ملی</strong>
                                <span style={s.value}>{organizerDetails.nationalCode || 'ثبت نشده'}</span>
                            </div>
                            <div style={s.infoBox}>
                                <strong style={s.label}>شماره موبایل</strong>
                                <span style={s.value}>{organizerDetails.phoneNumber || 'ثبت نشده'}</span>
                            </div>
                        </div>
                        <button onClick={() => setOrganizerDetails(null)}
                            style={{ width: '100%', marginTop: 20, padding: 10, background: '#64748b', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
                            بستن
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
