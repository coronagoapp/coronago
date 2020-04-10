package app.solocoin.solocoin.receiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import app.solocoin.solocoin.api.APIClient;
import app.solocoin.solocoin.api.APIService;
import app.solocoin.solocoin.app.SharedPref;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("LogNotTimber")
public class SessionPingManager extends Worker {

    public static final String TAG = SessionPingManager.class.getSimpleName();
    private APIService apiService;
    private SharedPref sharedPref;

    public SessionPingManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sharedPref = SharedPref.getInstance(context);
        apiService = APIClient.getRetrofitInstance(context).create(APIService.class);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.wtf("xolo", "doWork");
        final Result[] result = {Result.retry()};
        if (sharedPref.getSessionType() != null) {
            JsonObject body = new JsonObject();
            JsonObject session = new JsonObject();
            body.add("session", session);
            session.addProperty("type", sharedPref.getSessionType());
            Call<JsonObject> call = apiService.pingSession(sharedPref.getAuthToken(), body);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    JsonObject resp = response.body();
                    if (resp != null) {
                        sharedPref.setSessionStatus(resp.get("status").getAsString());
                        sharedPref.setSessionRewards(resp.get("rewards").getAsString());
                    }
                    result[0] = Result.success();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call,@NonNull Throwable t) {
                    result[0] = Result.failure();
                }
            });
        }
        return result[0];
    }
}
