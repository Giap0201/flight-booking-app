package com.example.flight_booking_app.booking.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.PassengerRequest;

import java.util.List;

public class PassengerAdapter extends BaseAdapter {

    private Context context;
    private List<PassengerRequest> listPassengers;
    private LayoutInflater inflater;

    public PassengerAdapter(Context context, List<PassengerRequest> listPassengers) {
        this.context = context;
        this.listPassengers = listPassengers;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return listPassengers.size();
    }

    @Override
    public Object getItem(int position) {
        return listPassengers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Dùng ViewHolder pattern để tối ưu hiệu suất ListView
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_passenger_form, parent, false);
            holder = new ViewHolder();
            holder.tvPassengerTitle = convertView.findViewById(R.id.tvPassengerTitle);
            holder.edtFirstName = convertView.findViewById(R.id.edtFirstName);
            holder.edtLastName = convertView.findViewById(R.id.edtLastName);
            holder.edtDateOfBirth = convertView.findViewById(R.id.edtDateOfBirth);
            holder.rgGender = convertView.findViewById(R.id.rgGender);

            // Gắn TextWatcher cho First Name
            holder.edtFirstName.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    // Khi người dùng gõ xong, lưu ngay vào Model ở vị trí tương ứng
                    listPassengers.get(holder.refPosition).setFirstName(s.toString());
                }
            });

            // Gắn TextWatcher cho Last Name
            holder.edtLastName.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    listPassengers.get(holder.refPosition).setLastName(s.toString());
                }
            });

            // Gắn TextWatcher cho Date Of Birth
            holder.edtDateOfBirth.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    listPassengers.get(holder.refPosition).setDateOfBirth(s.toString());
                }
            });

            // Gắn sự kiện chọn Giới tính
            holder.rgGender.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbMale) {
                    listPassengers.get(holder.refPosition).setGender("MALE");
                } else if (checkedId == R.id.rbFemale) {
                    listPassengers.get(holder.refPosition).setGender("FEMALE");
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Cập nhật vị trí hiện tại cho các sự kiện ở trên biết đường mà lưu data
        holder.refPosition = position;

        // Lấy đối tượng hành khách hiện tại
        PassengerRequest passenger = listPassengers.get(position);

        // Hiển thị tiêu đề (Ví dụ: Passenger 1 (ADULT))
        holder.tvPassengerTitle.setText("Passenger " + (position + 1) + " (" + passenger.getType() + ")");

        // Đổ dữ liệu từ Model ngược lại View (để khi cuộn ListView không bị mất chữ)
        // Lưu ý quan trọng: Phải gỡ TextWatcher ra trước khi setText nếu không nó sẽ bị loop, nhưng vì mình dùng refPosition nên có thể bỏ qua bước gỡ phức tạp.
        holder.edtFirstName.setText(passenger.getFirstName() != null ? passenger.getFirstName() : "");
        holder.edtLastName.setText(passenger.getLastName() != null ? passenger.getLastName() : "");
        holder.edtDateOfBirth.setText(passenger.getDateOfBirth() != null ? passenger.getDateOfBirth() : "");

        if ("FEMALE".equals(passenger.getGender())) {
            holder.rgGender.check(R.id.rbFemale);
        } else {
            holder.rgGender.check(R.id.rbMale); // Mặc định là MALE
        }

        return convertView;
    }

    // Lớp nội bộ để giữ các View, giúp ListView lướt mượt hơn
    private static class ViewHolder {
        TextView tvPassengerTitle;
        EditText edtFirstName;
        EditText edtLastName;
        EditText edtDateOfBirth;
        RadioGroup rgGender;
        int refPosition; // Biến này lưu lại vị trí của dòng hiện tại để lưu data đúng người
    }
}