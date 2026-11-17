package com.example.nutrivision.data.local.relation

import androidx.room.ColumnInfo

/**
 * "Vân tay" của một bảng: số dòng + thời điểm sửa mới nhất.
 *
 * Dùng để phát hiện dữ liệu vừa đổi mà không cần theo dõi từng ViewModel:
 * đồng bộ tự động chỉ cần so sánh vân tay, và việc đồng bộ tự cập nhật
 * `sync_status` (không đổi `updated_at`) nên không tạo vòng lặp vô hạn.
 */
data class DataFingerprint(
    @ColumnInfo(name = "row_count") val rowCount: Int,
    @ColumnInfo(name = "last_update") val lastUpdate: Long,
)
