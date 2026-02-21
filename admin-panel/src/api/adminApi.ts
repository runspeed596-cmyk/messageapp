import axios from 'axios';

export const BASE_URL = import.meta.env.VITE_API_URL || 'http://192.168.70.113:8080/api';
export const getMediaUrl = (path: string) => {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    const origin = BASE_URL.replace('/api', '');
    return `${origin}${path.startsWith('/') ? '' : '/'}${path}`;
};

const api = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Add a request interceptor to inject the JWT token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('admin_token');
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

export interface User {
    id: string;
    username: string;
    displayName: string;
    phoneNumber: string;
    avatarUrl: string;
    createdAt: string;
}

export interface HomeBanner {
    id?: string;
    title: string;
    imageUrl: string;
    linkUrl?: string;
    displayOrder: number;
    isActive: boolean;
}

export interface University {
    id?: string;
    name: string;
    country?: string;
    province?: string;
    city?: string;
    ministryName: string;
    type: string;
    establishedYear: number;
    studentCount: number;
    faculties?: string;
    departments?: string;
    iranRank?: number;
    worldRank?: number;
    articleCount?: number;
    journalCount?: number;
    facilities?: string;
    latitude: number;
    longitude: number;
    imageUrl?: string;
    websiteUrl?: string;
    honors?: string;
    professorCount?: number;
    professorNames?: string;
    publicationCount?: number;
    studentOrgs?: string;
    lastAdmissionCapacity?: string;
}

export interface EntertainmentMovie {
    id?: string;
    title: string;
    description?: string;
    videoUrl: string;
    thumbnailUrl?: string;
    duration?: string;
    releaseDate?: string;
    isActive: boolean;
}

export interface EntertainmentMusic {
    id?: string;
    title: string;
    artist?: string;
    audioUrl: string;
    coverUrl?: string;
    duration?: string;
    isActive: boolean;
}

export interface RiddleOption {
    text: string;
    displayOrder: number;
}

export interface EntertainmentRiddle {
    id?: string;
    title: string;
    description?: string;
    question: string;
    reward?: string;
    type: string;
    isMultipleChoice: boolean;
    options: RiddleOption[];
    correctAnswerIndex?: number;
    isActive: boolean;
}

export interface Discount {
    id?: string;
    title: string;
    code: string;
    percent: number;
    description?: string;
    imageUrl?: string;
    expiryDate?: string;
    category?: string;
}

export interface ElmEvent {
    id?: string;
    title: string;
    description: string;
    date: string;
    location: string;
    imageUrl?: string;
    organizer?: string;
    reward?: string;
    type: 'COMPETITION' | 'STARTUP' | 'CONGRESS';
    isExternal: boolean;
    link?: string;
    isApproved: boolean;
}

export interface FieldOfStudy {
    id?: string;
    name: string;
    educationLevel: string;
    displayOrder: number;
}

export interface EducationLevel {
    id?: string;
    name: string;
    displayOrder: number;
}

export interface Faculty {
    id?: string;
    name: string;
    displayOrder: number;
}

export type OfficialChannelCategory =
    | 'STUDENTS_IRAN' | 'MY_FIELD' | 'MY_UNIVERSITY' | 'MY_UNION'
    | 'FREELANCING' | 'PODCAST' | 'JOURNAL' | 'RESEARCH'
    | 'COMPETITIONS' | 'SCIENCE_TECH' | 'EDUCATION' | 'STUDENT_NEWS'
    | 'ENTERTAINMENT' | 'APP_OFFICIAL' | 'LOTTERY_DISCOUNT';

export type OfficialGroupCategory =
    | 'STUDENTS_IRAN' | 'MY_FIELD' | 'MY_UNIVERSITY'
    | 'MY_FIELD_UNIVERSITY' | 'MY_UNION' | 'TEACHERS' | 'QA_SCIENCE';

export interface AdminUser {
    id: string;
    username: string;
    displayName: string;
    phoneNumber?: string;
    avatarUrl?: string;
}

export interface OfficialChannel {
    id: string;
    name: string;
    description?: string;
    avatarUrl?: string;
    category: string;
    subscriberCount: number;
    admins: AdminUser[];
    displayMode: string;
    targetFieldOfStudy?: string;
    targetEducationLevel?: string;
    targetProvince?: string;
    targetCity?: string;
    targetUniversity?: string;
}

export interface OfficialGroup {
    id: string;
    name: string;
    description?: string;
    avatarUrl?: string;
    category: string;
    hideMembers: boolean;
    memberCount: number;
    admins: AdminUser[];
    displayMode: string;
    targetFieldOfStudy?: string;
    targetEducationLevel?: string;
    targetProvince?: string;
    targetCity?: string;
    targetUniversity?: string;
}

export interface CreateOfficialChannelRequest {
    name: string;
    description?: string;
    avatarUrl?: string;
    category: OfficialChannelCategory;
    displayMode: string;
    targetFieldOfStudy?: string;
    targetEducationLevel?: string;
    targetProvince?: string;
    targetCity?: string;
    targetUniversity?: string;
    adminIds?: string[];
}

export interface CreateOfficialGroupRequest {
    name: string;
    description?: string;
    avatarUrl?: string;
    category: OfficialGroupCategory;
    hideMembers: boolean;
    displayMode: string;
    targetFieldOfStudy?: string;
    targetEducationLevel?: string;
    targetProvince?: string;
    targetCity?: string;
    targetUniversity?: string;
    adminIds?: string[];
}

export interface AdRequest {
    id: string;
    requesterId: string;
    requesterName: string;
    requesterAvatar?: string;
    sourceMessageId: string;
    sourceType: string;
    sourceId?: string;
    targetChannelId: string;
    targetChannelName: string;
    targetChannelAvatar?: string;
    messageContent: string;
    messageMediaUrl?: string;
    messageType: string;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
    createdAt: string;
    reviewedAt?: string;
}

// File upload helper
const uploadFile = async (file: File, endpoint: string): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    const token = localStorage.getItem('admin_token');
    const response = await axios.post<ApiResponse<{ url: string }>>(
        `${BASE_URL}/admin/upload/${endpoint}`,
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': token ? `Bearer ${token}` : ''
            }
        }
    );
    return response.data.data.url;
};

export const adminApi = {
    // User management
    getUsers: () => api.get<ApiResponse<User[]>>('/admin/users'),
    deleteUser: (id: string) => api.delete<ApiResponse<void>>(`/admin/users/${id}`),

    // Banner management
    getBanners: () => api.get<ApiResponse<HomeBanner[]>>('/admin/banners'),
    saveBanner: (banner: HomeBanner) => api.post<ApiResponse<HomeBanner>>('/admin/banners', banner),
    deleteBanner: (id: string) => api.delete<ApiResponse<void>>(`/admin/banners/${id}`),

    // File uploads
    uploadBannerImage: (file: File) => uploadFile(file, 'banner'),
    uploadVideo: (file: File) => uploadFile(file, 'video'),
    uploadAudio: (file: File) => uploadFile(file, 'audio'),
    uploadImage: (file: File) => uploadFile(file, 'image'),

    // University management
    getUniversities: () => api.get<ApiResponse<University[]>>('/admin/universities'),
    saveUniversity: (uni: University) => api.post<ApiResponse<University>>('/admin/universities', uni),
    deleteUniversity: (id: string) => api.delete<ApiResponse<void>>(`/admin/universities/${id}`),

    // Location data
    getCountries: () => api.get<ApiResponse<string[]>>('/locations/countries'),
    getProvinces: (country: string) => api.get<ApiResponse<string[]>>(`/locations/provinces/${country}`),
    getCities: (province: string) => api.get<ApiResponse<string[]>>(`/locations/cities/${province}`),

    // Entertainment - Movies
    getMovies: () => api.get<ApiResponse<EntertainmentMovie[]>>('/admin/movies'),
    saveMovie: (movie: EntertainmentMovie) => api.post<ApiResponse<EntertainmentMovie>>('/admin/movies', movie),
    deleteMovie: (id: string) => api.delete<ApiResponse<void>>(`/admin/movies/${id}`),

    // Entertainment - Music
    getMusic: () => api.get<ApiResponse<EntertainmentMusic[]>>('/admin/music'),
    saveMusic: (music: EntertainmentMusic) => api.post<ApiResponse<EntertainmentMusic>>('/admin/music', music),
    deleteMusic: (id: string) => api.delete<ApiResponse<void>>(`/admin/music/${id}`),

    // Entertainment - Riddles
    getRiddles: () => api.get<ApiResponse<EntertainmentRiddle[]>>('/admin/riddles'),
    saveRiddle: (riddle: EntertainmentRiddle) => api.post<ApiResponse<EntertainmentRiddle>>('/admin/riddles', riddle),
    deleteRiddle: (id: string) => api.delete<ApiResponse<void>>(`/admin/riddles/${id}`),

    // Discounts
    getDiscounts: () => api.get<ApiResponse<Discount[]>>('/admin/discounts'),
    saveDiscount: (discount: Discount) => api.post<ApiResponse<Discount>>('/admin/discounts', discount),
    deleteDiscount: (id: string) => api.delete<ApiResponse<void>>(`/admin/discounts/${id}`),

    // Competitions / ElmPeak
    getEvents: () => api.get<ApiResponse<ElmEvent[]>>('/elm-peak/events'),
    getPendingEvents: () => api.get<ApiResponse<ElmEvent[]>>('/admin/elm-peak/pending'),
    approveEvent: (id: string) => api.put<ApiResponse<void>>(`/admin/elm-peak/${id}/approve`),
    rejectEvent: (id: string) => api.delete<ApiResponse<void>>(`/admin/elm-peak/${id}`),
    saveEvent: (event: ElmEvent) => api.post<ApiResponse<ElmEvent>>('/admin/elm-peak', event),

    // Fields of Study management
    getFieldsOfStudy: () => api.get<ApiResponse<FieldOfStudy[]>>('/admin/fields-of-study'),
    saveFieldOfStudy: (field: FieldOfStudy) => api.post<ApiResponse<FieldOfStudy>>('/admin/fields-of-study', field),
    deleteFieldOfStudy: (id: string) => api.delete<ApiResponse<void>>(`/admin/fields-of-study/${id}`),

    // Education Level management
    getEducationLevels: () => api.get<ApiResponse<EducationLevel[]>>('/admin/education-levels'),
    saveEducationLevel: (level: EducationLevel) => api.post<ApiResponse<EducationLevel>>('/admin/education-levels', level),
    deleteEducationLevel: (id: string) => api.delete<ApiResponse<void>>(`/admin/education-levels/${id}`),

    // Faculty management
    getFaculties: () => api.get<ApiResponse<Faculty[]>>('/admin/faculties'),
    saveFaculty: (faculty: Faculty) => api.post<ApiResponse<Faculty>>('/admin/faculties', faculty),
    deleteFaculty: (id: string) => api.delete<ApiResponse<void>>(`/admin/faculties/${id}`),

    // Official Channels management
    getOfficialChannels: () => api.get<ApiResponse<OfficialChannel[]>>('/admin/special-folder/official-channels'),
    createOfficialChannel: (req: CreateOfficialChannelRequest) => api.post<ApiResponse<OfficialChannel>>('/admin/special-folder/official-channels', req),
    updateOfficialChannel: (id: string, req: CreateOfficialChannelRequest) => api.put<ApiResponse<OfficialChannel>>(`/admin/special-folder/official-channels/${id}`, req),
    deleteOfficialChannel: (id: string) => api.delete<ApiResponse<void>>(`/admin/special-folder/official-channels/${id}`),
    getChannelAdmins: (channelId: string) => api.get<ApiResponse<AdminUser[]>>(`/admin/special-folder/official-channels/${channelId}/admins`),
    addChannelAdmin: (channelId: string, userId: string) => api.post<ApiResponse<void>>(`/admin/special-folder/official-channels/${channelId}/admins`, { userId }),
    removeChannelAdmin: (channelId: string, userId: string) => api.delete<ApiResponse<void>>(`/admin/special-folder/official-channels/${channelId}/admins/${userId}`),

    // Official Groups management
    getOfficialGroups: () => api.get<ApiResponse<OfficialGroup[]>>('/admin/special-folder/official-groups'),
    createOfficialGroup: (req: CreateOfficialGroupRequest) => api.post<ApiResponse<OfficialGroup>>('/admin/special-folder/official-groups', req),
    updateOfficialGroup: (id: string, req: CreateOfficialGroupRequest) => api.put<ApiResponse<OfficialGroup>>(`/admin/special-folder/official-groups/${id}`, req),
    deleteOfficialGroup: (id: string) => api.delete<ApiResponse<void>>(`/admin/special-folder/official-groups/${id}`),
    getGroupAdmins: (groupId: string) => api.get<ApiResponse<AdminUser[]>>(`/admin/special-folder/official-groups/${groupId}/admins`),
    addGroupAdmin: (groupId: string, userId: string) => api.post<ApiResponse<void>>(`/admin/special-folder/official-groups/${groupId}/admins`, { userId }),
    removeGroupAdmin: (groupId: string, userId: string) => api.delete<ApiResponse<void>>(`/admin/special-folder/official-groups/${groupId}/admins/${userId}`),

    // Auth
    login: (credentials: any) => api.post<ApiResponse<{ token: string }>>('/admin/auth/login', credentials),

    // Advertisement management
    getAdRequests: (status?: string) => api.get<ApiResponse<{ adRequests: AdRequest[]; totalCount: number }>>('/admin/ads', { params: { status } }),
    approveAd: (id: string) => api.put<ApiResponse<void>>(`/admin/ads/${id}/approve`),
    rejectAd: (id: string) => api.put<ApiResponse<void>>(`/admin/ads/${id}/reject`),
};

export default api;

