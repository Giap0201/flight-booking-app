package com.example.flight_booking_app.ticket.activity; // Khuyên dùng: Sau này bạn nên move file này sang package 'ticket.fragment' cho chuẩn nhé

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // ĐÃ ĐỔI TỪ AppCompatActivity SANG Fragment
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.PendingTicketAdapter;
import com.example.flight_booking_app.ticket.adapter.UpcomingTicketAdapter;
import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.model.BookingSummary;
import com.example.flight_booking_app.ticket.model.PageResult;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.AppConfig;
import com.example.flight_booking_app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ĐÃ SỬA: extends Fragment thay vì AppCompatActivity
public class MyTicketFragment extends Fragment {

    private RecyclerView rvPendingTickets, rvUpcomingTickets;
    private ImageView btnHistory;
    private View layoutUpcomingHeader, layoutWaitingHeader;

    private PendingTicketAdapter pendingAdapter;
    private UpcomingTicketAdapter upcomingAdapter;

    private List<BookingSummary> pendingList = new ArrayList<>();
    private List<BookingSummary> upcomingList = new ArrayList<>();

    // Constructor rỗng bắt buộc cho Fragment
    public MyTicketFragment() {
    }

    // ĐÃ SỬA: Dùng onCreateView để "thổi" layout (thay cho setContentView)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_ticket, container, false);
    }

    // ĐÃ SỬA: Viết logic code ở onViewCreated
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view); // Truyền 'view' vào để findViewById
        setupRecyclerViews();

        // Gọi API lấy dữ liệu
        fetchPendingTickets();
        fetchUpcomingTickets();
    }

    // ĐÃ SỬA: Thêm tham số View và dùng view.findViewById
    private void initViews(View view) {
        rvPendingTickets = view.findViewById(R.id.rvPendingTickets);
        rvUpcomingTickets = view.findViewById(R.id.rvUpcomingTickets);
        btnHistory = view.findViewById(R.id.btnHistory);
        layoutUpcomingHeader = view.findViewById(R.id.layoutUpcomingHeader);
        layoutWaitingHeader = view.findViewById(R.id.layoutWaitingHeader);

        // ĐÃ SỬA: Thay 'this' bằng 'requireContext()' trong Intent
        btnHistory.setOnClickListener(v -> startActivity(new Intent(requireContext(), HistoryActivity.class)));

        if (layoutUpcomingHeader != null) {
            layoutUpcomingHeader.setOnClickListener(v -> startActivity(new Intent(requireContext(), UpcomingFlightsActivity.class)));
        }
        if (layoutWaitingHeader != null) {
            layoutWaitingHeader.setOnClickListener(v -> startActivity(new Intent(requireContext(), WaitingForPaymentActivity.class)));
        }
    }

    private void setupRecyclerViews() {
        // ĐÃ SỬA: Thay 'this' bằng 'requireContext()'
        rvPendingTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        pendingAdapter = new PendingTicketAdapter(pendingList);
        rvPendingTickets.setAdapter(pendingAdapter);

        // ĐÃ SỬA: Thay 'this' bằng 'requireContext()'
        rvUpcomingTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingAdapter = new UpcomingTicketAdapter(upcomingList);
        rvUpcomingTickets.setAdapter(upcomingAdapter);
    }

    // 1. Logic lấy vé chờ thanh toán
    private void fetchPendingTickets() {
        // ĐÃ SỬA: Thay 'this' bằng 'requireContext()'
        TicketApiService apiService = ApiClient.getClient(requireContext()).create(TicketApiService.class);

        apiService.getMyBookingsWithFilter(AppConfig.TOKEN, "ALL", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingSummary> allData = response.body().getResult().getData();
                    pendingList.clear();

                    if (allData != null) {
                        for (BookingSummary item : allData) {
                            String status = item.getStatus();
                            if ("PENDING".equals(status) || "AWAITING_PAYMENT".equals(status)) {
                                pendingList.add(item);
                            }
                            if (pendingList.size() == 2) break;
                        }
                    }
                    pendingAdapter.setTicketList(pendingList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("MyTicket", "Error Fetching Pending: " + t.getMessage());
            }
        });
    }

    // 2. Logic lấy vé sắp bay
    private void fetchUpcomingTickets() {
        // ĐÃ SỬA: Thay 'this' bằng 'requireContext()'
        TicketApiService apiService = ApiClient.getClient(requireContext()).create(TicketApiService.class);

        apiService.getMyBookingsWithFilter(AppConfig.TOKEN, "UPCOMING", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingSummary> data = response.body().getResult().getData();
                    upcomingList.clear();

                    if (data != null) {
                        for (int i = 0; i < Math.min(data.size(), 2); i++) {
                            upcomingList.add(data.get(i));
                        }
                    }
                    upcomingAdapter.setTicketList(upcomingList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("MyTicket", "Error Fetching Upcoming: " + t.getMessage());
            }
        });
    }
}