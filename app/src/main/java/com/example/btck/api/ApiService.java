package com.example.btck.api;

import com.example.btck.models.*;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ============ AUTH ============
    @FormUrlEncoded
    @POST("auth/login")
    Call<TokenResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    @POST("auth/register")
    Call<UserPublic> register(@Body UserRegister body);

    @POST("auth/refresh")
    Call<TokenResponse> refreshToken(@Body RefreshTokenRequest body);

    @POST("auth/logout")
    Call<MessageResponse> logout();

    @POST("auth/password-recovery/{email}")
    Call<MessageResponse> recoverPassword(@Path("email") String email);

    @POST("auth/reset-password/")
    Call<MessageResponse> resetPassword(@Body NewPasswordRequest body);

    @GET("auth/me")
    Call<UserPublic> getMe();

    // ============ USERS ============
    @GET("users/me")
    Call<UserPublic> getCurrentUser();

    @PATCH("users/me")
    Call<UserPublic> updateMe(@Body UpdateUserMeRequest body);

    @PATCH("users/me/password")
    Call<MessageResponse> updatePassword(@Body UpdatePasswordRequest body);

    @DELETE("users/me")
    Call<MessageResponse> deleteMe();

    @Multipart
    @POST("users/me/avatar")
    Call<UserPublic> uploadAvatar(@Part MultipartBody.Part file);

    @POST("users/me/fcm-token")
    Call<MessageResponse> registerFcmToken(@Body FCMTokenRequest body);

    @DELETE("users/me/fcm-token/{token}")
    Call<MessageResponse> unregisterFcmToken(@Path("token") String token);

    @GET("users/search")
    Call<List<UserPublic>> searchUsers(@Query("email") String email);

    @GET("users/{user_id}/payment-qr")
    Call<ResponseBody> getPaymentQr(
            @Path("user_id") String userId,
            @Query("amount") long amount,
            @Query("description") String description
    );

    // ============ UTILS ============
    @GET("utils/banks")
    Call<BanksResponse> getBanks();

    // ============ EVENTS (Groups) ============
    @GET("events/")
    Call<EventsPublic> getEvents(
            @Query("skip") int skip,
            @Query("limit") int limit,
            @Query("q") String query
    );

    @POST("events/")
    Call<EventPublic> createEvent(@Body EventCreate body);

    @GET("events/{event_id}")
    Call<EventPublic> getEvent(@Path("event_id") String eventId);

    @PUT("events/{event_id}")
    Call<EventPublic> updateEvent(
            @Path("event_id") String eventId,
            @Body EventCreate body
    );

    @DELETE("events/{event_id}")
    Call<MessageResponse> deleteEvent(@Path("event_id") String eventId);

    @GET("events/me/balance")
    Call<MyBalanceDetail> getMyBalance();

    // Members
    @POST("events/{event_id}/members")
    Call<EventMemberPublic> addMember(
            @Path("event_id") String eventId,
            @Body AddMemberRequest body
    );

    @DELETE("events/{event_id}/members/{user_id}")
    Call<MessageResponse> removeMember(
            @Path("event_id") String eventId,
            @Path("user_id") String userId
    );

    @GET("events/{event_id}/balances")
    Call<EventBalances> getEventBalances(@Path("event_id") String eventId);

    @GET("events/{event_id}/balances/simplify")
    Call<SimplifiedDebtsResponse> getSimplifiedDebts(@Path("event_id") String eventId);

    @GET("events/{event_id}/stats")
    Call<EventStats> getEventStats(@Path("event_id") String eventId);

    @POST("events/{event_id}/invite")
    Call<InviteCodePublic> createInviteCode(
            @Path("event_id") String eventId,
            @Body InviteCodeCreate body
    );

    @POST("events/join/{code}")
    Call<EventMemberPublic> joinEventByCode(@Path("code") String code);

    // ============ EXPENSES ============
    @GET("events/{event_id}/expenses/")
    Call<ExpensesPublic> getExpenses(
            @Path("event_id") String eventId,
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @POST("events/{event_id}/expenses/")
    Call<ExpensePublic> createExpense(
            @Path("event_id") String eventId,
            @Body ExpenseCreate body
    );

    @GET("events/{event_id}/expenses/{expense_id}")
    Call<ExpensePublic> getExpense(
            @Path("event_id") String eventId,
            @Path("expense_id") String expenseId
    );

    @PUT("events/{event_id}/expenses/{expense_id}")
    Call<ExpensePublic> updateExpense(
            @Path("event_id") String eventId,
            @Path("expense_id") String expenseId,
            @Body ExpenseUpdate body
    );

    @DELETE("events/{event_id}/expenses/{expense_id}")
    Call<MessageResponse> deleteExpense(
            @Path("event_id") String eventId,
            @Path("expense_id") String expenseId
    );

    @Multipart
    @POST("events/{event_id}/expenses/{expense_id}/image")
    Call<ExpensePublic> uploadExpenseImage(
            @Path("event_id") String eventId,
            @Path("expense_id") String expenseId,
            @Part MultipartBody.Part file
    );

    // ============ SETTLEMENTS ============
    @GET("events/{event_id}/settlements/")
    Call<SettlementsPublic> getSettlements(
            @Path("event_id") String eventId,
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @POST("events/{event_id}/settlements/")
    Call<SettlementPublic> createSettlement(
            @Path("event_id") String eventId,
            @Body SettlementCreate body
    );

    @GET("events/{event_id}/settlements/{settlement_id}")
    Call<SettlementPublic> getSettlement(
            @Path("event_id") String eventId,
            @Path("settlement_id") String settlementId
    );

    @DELETE("events/{event_id}/settlements/{settlement_id}")
    Call<MessageResponse> deleteSettlement(
            @Path("event_id") String eventId,
            @Path("settlement_id") String settlementId
    );

    // ============ NOTIFICATIONS ============
    @GET("notifications/")
    Call<NotificationsPublic> getNotifications(
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @GET("notifications/unread-count")
    Call<UnreadCountResponse> getUnreadCount();

    @PATCH("notifications/{notification_id}/read")
    Call<NotificationPublic> markNotificationRead(
            @Path("notification_id") String notificationId
    );

    @POST("notifications/mark-all-read")
    Call<MessageResponse> markAllRead();
}
