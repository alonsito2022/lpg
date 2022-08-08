package com.example.driver.retrofit
import com.example.driver.model.*
import com.example.driver.rest.RequestDispatch
import com.example.driver.rest.RequestDispatchDetail
import com.example.driver.rest.ApiResponse
import com.example.driver.rest.RequestPaymentMethod
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ClientService {

    @GET("get_clients_and_address/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getClientsAndAddresses() : Call<ArrayList<Client>>

    @GET("commercial/api/v1/get_zones/{text_match}")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun searchZones(@Path("text_match") text_match: String?) : Call<ArrayList<Zone>>

    @GET("commercial/api/v1/search_clients_and_address/{text_match}")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun searchClientsAndAddresses(@Path("text_match") text_match: String?) : Call<ArrayList<Client>>

    @GET("get_debtors_by_driver/{id}")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getDebtors(@Path("id") id: Int?) : Call<ArrayList<Client>>

    @GET("commercial/api/v1/get_debtors_with_debt_by_driver/{id}")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getDebtorsWithDebt(@Path("id") id: Int?) : Call<ArrayList<Debt>>

    @GET("get_debtor/{driverID}/{clientID}")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getProductsOwed(@Path("driverID") driverID: Int?, @Path("clientID") clientID: Int?) : Call<ArrayList<Debt>>

    @POST("dispatches/")
    @Headers("Content-type:application/json")
    fun createDispatch(@Body params: RequestDispatch): Call<RequestDispatch>

    @POST("dispatch_details/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun createDispatchDetail(@Body params: RequestDispatchDetail): Call<RequestDispatchDetail>


    @POST("commercial/api/v1/register_dispatch/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun registerDispatch(@Body params: Dispatch): Call<ApiResponse>

    @POST("commercial/api/v2/register_address/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun registerClientAddress(@Body params: Client): Call<ApiResponse>

    @POST("commercial/api/v1/update_dispatch_from_mobile/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun updateDispatch(@Body params: Dispatch): Call<ApiResponse>

    @POST("commercial/api/v1/register_or_update_device/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun registerOrUpdateDevice(@Body params: Device): Call<ApiResponse>

    @POST("commercial/api/v1/register_recovery_from_creditor/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun registerRecoveryFromCreditor(@Body params: Recovery): Call<ApiResponse>

    @POST("get_sales_in_cash/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getPaymentMethods(@Body params: Driver): Call<RequestPaymentMethod>

    @GET("get_products/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getProducts(): Call<ArrayList<Product>>

    @POST("get_inventory/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getInventory(@Body params: Vehicle): Call<RequestPaymentMethod>

    @POST("users/api/v2/search_driver_by_credentials/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun searchDriverByCredentials(@Body params: Driver): Call<Driver>

    @POST("users/api/v2/search_driver_by_id/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun searchDriverByID(@Body params: Driver): Call<Driver>

    @POST("accounting/api/v2/get_cashes/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getCashes(@Body params: Driver) : Call<ArrayList<Cash>>

    @POST("accounting/api/v2/get_cash_flow/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getCashFlow(@Body params: CashFlow) : Call<ArrayList<CashFlow>>

    @POST("accounting/api/v2/register_cash_flow/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun saveCashFlow(@Body params: CashFlow) : Call<ArrayList<CashFlow>>

    @POST("commercial/api/v2/get_dispatches_by_date/")
    @Headers("Accept: application/json", "Content-type:application/json")
    fun getDispatchesByDate(@Body params: Dispatch): Call<ArrayList<Dispatch>>

    companion object {

//        var BASE_URL = "https://38.242.197.197:9015/commercial/api/v1/"
         var BASE_URL = "https://www.camotegas.ml/"
//        var BASE_URL = "http://192.168.1.22:8000/"
//        var BASE_URL = "http://192.168.1.14:8000/commercial/api/v1/"
//        var BASE_URL = "http://192.168.1.15:8000/commercial/api/v1/"

        fun create() : ClientService {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ClientService::class.java)

        }
    }
}