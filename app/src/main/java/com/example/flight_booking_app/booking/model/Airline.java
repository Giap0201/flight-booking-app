    package com.example.flight_booking_app.booking.model;

    import com.google.gson.annotations.SerializedName;
    import java.io.Serializable;

    // Class này đại diện cho cục "airline" trong JSON
    public class Airline implements Serializable {

        @SerializedName("code")
        private String code; // Mã hãng (VD: VJ, VN...)

        @SerializedName("name")
        private String name; // Tên hãng bay để hiển thị lên UI (VD: VietJet Air, Indonesia Air Asia)

        @SerializedName("logoUrl")
        private String logoUrl; // Link ảnh logo của hãng

        // Constructor rỗng bắt buộc cho Gson
        public Airline() {
        }

        public Airline(String code, String name, String logoUrl) {
            this.code = code;
            this.name = name;
            this.logoUrl = logoUrl;
        }

        // --- GETTER & SETTER ---

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }
    }