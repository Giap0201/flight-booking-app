package com.example.flight_booking_app.ticket.response;

/**
 * POJO đại diện cho một giao dịch thanh toán (Transaction) trong chi tiết booking.
 * Được parse từ mảng "transactions" trong JSON response của GET /bookings/{id}.
 *
 * Ví dụ JSON:
 * {
 *   "amount": 5000000,
 *   "paymentMethod": "CREDIT_CARD",
 *   "status": "SUCCESS",
 *   "transactionNo": "TXN123456",
 *   "createdAt": "2026-04-06T10:20:51.576Z"
 * }
 */
public class TransactionResponse {

    private double amount;          // Số tiền giao dịch
    private String paymentMethod;   // Phương thức thanh toán (VD: "CREDIT_CARD", "BANK_TRANSFER")
    private String status;          // Trạng thái giao dịch (VD: "SUCCESS", "FAILED", "PENDING")
    private String transactionNo;   // Mã giao dịch duy nhất
    private String createdAt;       // Thời gian tạo giao dịch (ISO 8601 format)

    // --- GETTERS ---

    public double getAmount() { return amount; }

    public String getPaymentMethod() { return paymentMethod; }

    public String getStatus() { return status; }

    public String getTransactionNo() { return transactionNo; }

    public String getCreatedAt() { return createdAt; }
}
