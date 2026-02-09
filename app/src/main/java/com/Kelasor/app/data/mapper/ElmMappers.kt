package com.Kelasor.app.data.mapper

import com.Kelasor.app.data.Coordinates
import com.Kelasor.app.data.University
import com.Kelasor.app.data.remote.dto.UniversityDto

fun UniversityDto.toDomain() = University(
    id = id,
    name = name,
    coordinates = Coordinates(latitude, longitude),
    province = province ?: "",
    city = city ?: "",
    ministry = ministryName ?: "وزارت علوم، تحقیقات و فناوری",
    type = type ?: "دولتی",
    establishmentYear = establishedYear?.toString() ?: "۱۳۱۳",
    studentCount = studentCount,
    faculties = faculties?.split(",")?.map { it.trim() } ?: emptyList(),
    majors = departments?.split(",")?.map { it.trim() } ?: emptyList(),
    iranRank = iranRank ?: 0,
    worldRank = worldRank ?: 0,
    paperCount = articleCount,
    journalCount = journalCount,
    facilities = facilities?.split(",")?.map { it.trim() } ?: emptyList()
)
