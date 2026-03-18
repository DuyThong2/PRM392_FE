package com.example.prm392fe.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392fe.R;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.adapter.LocationAdapter;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.databinding.FragmentMapsBinding;
import com.example.prm392fe.models.responses.LocationResponse;
import com.example.prm392fe.repositories.LocationRepository;
import com.example.prm392fe.utils.MyUtils;
import com.example.prm392fe.viewModel.LocationViewModel;
import com.example.prm392fe.viewModel.factory.LocationViewModelFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapsFragment extends Fragment {
    String TAG = "MAPS_FRAGMENT";
    MapView map = null; // Khai báo đối tượng MapView
    MyLocationNewOverlay myLocationOverlay; // Đối tượng hiển thị vị trí người dùng
    Marker destinationMarker = null;
    LocationAdapter locationAdapter = new LocationAdapter(new ArrayList<>());
    LocationViewModel locationViewModel;
    FragmentMapsBinding binding;

    // Khai báo launcher để xử lý yêu cầu quyền
    private ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    // Quyền đã được cấp, khởi tạo định vị
                    setupMyLocationOverlay();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Quyền truy cập vị trí thô được cấp, vẫn có thể dùng nhưng kém chính xác hơn
                    setupMyLocationOverlay();
                } else {
                    // Người dùng từ chối quyền
                    // Tùy chọn: Hiển thị thông báo yêu cầu người dùng bật quyền
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1. Cấu hình OSMDroid (RẤT QUAN TRỌNG)
        // Thiết lập Context và User Agent. Phải làm điều này trước khi khởi tạo MapView
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx));
        // Đặt User Agent theo khuyến nghị
        Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());

        binding = FragmentMapsBinding.inflate(inflater, container, false);
        return binding.getRoot(); // đây là "View" của Fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Lấy tham chiếu đến MapView từ layout
        map = binding.map;

        // 3. Cấu hình cơ bản cho bản đồ
        map.setTileSource(TileSourceFactory.MAPNIK); // Đặt nguồn tile là OSM Standard
        map.setMultiTouchControls(true); // Cho phép zoom bằng 2 ngón tay

        // Kiểm tra và yêu cầu quyền định vị
        if (checkLocationPermissions()) {
            setupMyLocationOverlay();
        } else {
            requestLocationPermissions();
        }

        if (myLocationOverlay == null || !myLocationOverlay.isMyLocationEnabled() || myLocationOverlay.getLastFix() == null) {
            map.getController().setZoom(17.0); // Độ zoom
//            // 5. Thêm Marker (điểm đánh dấu)
//            Marker startMarker = new Marker(map);
//            startMarker.setPosition(startPoint);
//            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            startMarker.setTitle("Marker in Ho Chi Minh City");
//            map.getOverlays().add(startMarker);
//            map.invalidate(); // Vẽ lại bản đồ để hiển thị marker }
        }

        // Nút "Vị trí của tôi"
        // tự chuyển snake_case -> camelCase
        ImageButton btnCenter = binding.btnCenterLocation;
        btnCenter.setOnClickListener(v -> centerMapOnUserLocation());

        ImageButton btnSearch = binding.btnSearchLocation;
        EditText editSearch = binding.editTextSearch;

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick btnSearch: "+editSearch.getText().toString());
                searchLocationByName(editSearch.getText().toString());
            }
        });

        RecyclerView locationRV = binding.locationRV;
        locationRV.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        locationRV.setAdapter(locationAdapter);

        LocationRepository repository = new LocationRepository(ApiClient.getApiService(), SessionManager.getInstance(requireContext()));
        LocationViewModelFactory factory = new LocationViewModelFactory(repository);
        locationViewModel = new ViewModelProvider(this, factory).get(LocationViewModel.class);

        locationViewModel.getLocations().observe(getViewLifecycleOwner(), new Observer<List<LocationResponse>>() {
            @Override
            public void onChanged(List<LocationResponse> locations) {
                if (locations != null) {
                    locationAdapter.setLocations(locations);
                }
            }
        });

        locationAdapter.setOnItemClickListener(location -> {
            Log.i(TAG, "Location clicked: " + location);

            if (myLocationOverlay != null && myLocationOverlay.getLastFix() != null) {
                double currentLat = myLocationOverlay.getLastFix().getLatitude();
                double currentLon = myLocationOverlay.getLastFix().getLongitude();

                double destLat = location.getLatitude().doubleValue();
                double destLon = location.getLongitude().doubleValue();

                drawRoute(currentLon, currentLat, destLon, destLat);
            } else {
                Log.w(TAG, "Chưa có vị trí hiện tại hoặc GPS chưa sẵn sàng");
            }
        });
    }

    // --- Các Phương thức mới ---
    private void drawRoute(double startLon, double startLat, double endLon, double endLat) {
        OkHttpClient client = new OkHttpClient();

        // SỬA ĐỔI QUAN TRỌNG: Thêm Locale.US vào String.format
        String url = String.format(
                Locale.US, // <-- Ép buộc sử dụng Locale US để đảm bảo dấu chấm thập phân
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                startLon,
                startLat,
                endLon,
                endLat
        );

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "GundamSaleApp/1.0")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JSONObject jsonObj = new JSONObject(json);
                    JSONArray routes = jsonObj.getJSONArray("routes");

                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject geometry = route.getJSONObject("geometry");

                        // Lấy mảng tọa độ GeoJSON
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        List<GeoPoint> geoPoints = new ArrayList<>();

                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray coord = coordinates.getJSONArray(i);
                            double lon = coord.getDouble(0);
                            double lat = coord.getDouble(1);
                            geoPoints.add(new GeoPoint(lat, lon));
                        }

                        requireActivity().runOnUiThread(() -> {
                            // Xóa đường cũ nếu có
                            map.getOverlays().removeIf(o -> o instanceof org.osmdroid.views.overlay.Polyline);

                            // Xóa marker cũ nếu có
                            if (destinationMarker != null) {
                                map.getOverlays().remove(destinationMarker);
                                destinationMarker = null;  // Đặt lại marker destinationMarker về null
                            }

                            // Tạo polyline mới
                            org.osmdroid.views.overlay.Polyline line = new org.osmdroid.views.overlay.Polyline();
                            line.setPoints(geoPoints);
                            line.setColor(ContextCompat.getColor(requireContext(), R.color.colorSuccess));
                            line.setWidth(8f);

                            map.getOverlays().add(line);

                            if (!geoPoints.isEmpty()) {
                                GeoPoint destinationPoint = geoPoints.get(geoPoints.size() - 1); // Lấy điểm cuối cùng

                                destinationMarker = new Marker(map);
                                destinationMarker.setPosition(destinationPoint);
                                destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                                // Đặt tiêu đề (Có thể lấy từ LocationResponse nếu bạn truyền vào)
                                destinationMarker.setTitle("Điểm đích");

                                // Tùy chọn: Sử dụng một icon khác biệt cho điểm đích
                                Drawable icon = MyUtils.getScaledVectorDrawable(requireContext(), R.drawable.ic_location_red, 48);
                                destinationMarker.setIcon(icon);

                                map.getOverlays().add(destinationMarker);
                            }

                            map.invalidate();

                            // Zoom vừa đủ để thấy toàn tuyến đường
                            map.zoomToBoundingBox(org.osmdroid.util.BoundingBox.fromGeoPointsSafe(geoPoints), true);
                        });
                    }
                } else {
                    Log.e(TAG, "Route API failed: " + response);
                }
            } catch (Exception e) {
                Log.e(TAG, "drawRoute error: " + e.getMessage());
            }
        }).start();
    }

    private void searchLocationByName(String query) {
        OkHttpClient client = new OkHttpClient();

        // 👉 URL encode để tránh lỗi với dấu cách hoặc ký tự tiếng Việt
        String encodedQuery = Uri.encode(query);
        String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&limit=5";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "GundamSaleApp/1.0") // ⚠️ bắt buộc
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    JSONArray results = new JSONArray(jsonData);

                    if (results.length() > 0) {
                        JSONObject firstResult = results.getJSONObject(0);
                        double lat = firstResult.getDouble("lat");
                        double lon = firstResult.getDouble("lon");
                        String displayName = firstResult.getString("display_name");

                        Log.d("Nominatim", "Kết quả: " + displayName + " (" + lat + ", " + lon + ")");

                        requireActivity().runOnUiThread(() -> {
                            GeoPoint point = new GeoPoint(lat, lon);
                            map.getController().setCenter(point);
                            map.getController().setZoom(17.0);

                            // Thêm marker
                            Marker marker = new Marker(map);
                            marker.setPosition(point);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setTitle(displayName);

                            Drawable icon = MyUtils.getScaledVectorDrawable(requireContext(), R.drawable.ic_location_red, 48);
                            marker.setIcon(icon);

                            map.getOverlays().add(marker);
                            map.invalidate();
                        });
                    } else {
                        Log.d("Nominatim", "Không tìm thấy địa điểm");
                    }
                }
            } catch (Exception e) {
                Log.e("Nominatim", "Lỗi khi tìm kiếm: " + e.getMessage());
            }
        }).start();
    }

    private void centerMapOnUserLocation() {
        if (myLocationOverlay != null && myLocationOverlay.isMyLocationEnabled() && myLocationOverlay.getLastFix() != null) {
            // Lấy tọa độ cuối cùng đã biết
            GeoPoint myCurrentLocation = new GeoPoint(myLocationOverlay.getLastFix());

            // Di chuyển bản đồ đến vị trí đó
            map.getController().animateTo(myCurrentLocation);
            map.getController().setZoom(17.0); // Thường zoom sát hơn khi tìm về vị trí hiện tại
        } else {
            // Tùy chọn: Xử lý khi chưa có vị trí (ví dụ: thông báo yêu cầu bật GPS)
            // Toast.makeText(requireContext(), "Không tìm thấy vị trí hiện tại. Đang chờ tín hiệu GPS.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void setupMyLocationOverlay() {
        // Tạo nhà cung cấp GPS cho OSMDroid
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(requireContext());
        // Tạo lớp phủ hiển thị vị trí của tôi
        myLocationOverlay = new MyLocationNewOverlay(locationProvider, map);

        Bitmap personBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_marker);
        int width = (int) (personBitmap.getWidth() * 0.15f);
        int height = (int) (personBitmap.getHeight() * 0.15f);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(personBitmap, width, height, true);

        myLocationOverlay.setPersonIcon(scaledBitmap);
        myLocationOverlay.setPersonAnchor(0.5f, 0.7f);


        // Bật lớp phủ và theo dõi vị trí
        myLocationOverlay.enableMyLocation();
        // Tùy chọn: Tự động di chuyển bản đồ theo vị trí người dùng
        myLocationOverlay.enableFollowLocation();
        // Thêm lớp phủ vào bản đồ
        map.getOverlays().add(myLocationOverlay);

        // Di chuyển bản đồ đến vị trí hiện tại ngay lập tức (nếu đã có vị trí)
        if (myLocationOverlay.isMyLocationEnabled() && myLocationOverlay.getLastFix() != null) {
            GeoPoint myCurrentLocation = new GeoPoint(myLocationOverlay.getLastFix());
            map.getController().animateTo(myCurrentLocation);
            map.getController().setZoom(17.0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        // Kích hoạt lại việc theo dõi vị trí khi fragment resume
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
        } else {
            requestLocationPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
        // Tạm dừng theo dõi vị trí khi fragment pause để tiết kiệm pin
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }
}