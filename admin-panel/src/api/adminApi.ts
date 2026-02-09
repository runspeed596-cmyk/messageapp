import axios from 'axios';

export const BASE_URL = import.meta.env.VITE_API_URL || 'http://46.249.100.239/api';
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

    // Auth
    login: (credentials: any) => api.post<ApiResponse<{ token: string }>>('/admin/auth/login', credentials),
};

export default api;

