package it.rasxodbot.Entity.Enum;

public enum UserState {
    NONE,

    // ===== REGISTER =====
    WAITING_NAME,
    WAITING_PHONE_NUMBER,
    // ===== KIRIM =====
    WAITING_KIRIM_MIQDOR,
    WAITING_KIRIM_SABAB,

    // ===== CHIQIM =====
    WAITING_CHIQIM_NOMI,

    // ===== DAILY_CHIQIM =====
    WAITING_DAILY_MIQDOR,

    WAITING_DAILY_DESCRIPTION,

    // ===== SEARCH =====
    WAITING_SEARCH,

    // ===== SEARCH_FOR_CHIQIM =====
    WAITING_SEARCH_CHIQIM,

    // ===== SET_NOTIFICATION_TIME =====
    WAITING_NOTIFICATION_TIME
}
