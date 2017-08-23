package com.example.g14.financesdemo

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.g14.financesdemo.model.Currency
import com.example.g14.financesdemo.model.MoneyAmount
import com.example.g14.financesdemo.model.Transaction
import com.example.g14.financesdemo.network.ApiService
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Gabriel Fortin
 */

fun MainActivity.performRetrofitLogin() {
    val longSnack = { msg: String ->
        Snackbar.make(fab, msg, Snackbar.LENGTH_LONG).show()
    }

    ApiService.create()
            .login()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { longSnack("TOKEN: ${it.token}"); Log.d("A", "$it") },
                    { longSnack("ERROR: ${it.message}"); Log.e("A", "$it") }
            )
}

val MainActivity.dataManager by lazy {
    val loginNetworkCall = ApiService.create()
            .login()
            .map { it.token }
    DataManagerImpl(loginNetworkCall)
}

fun MainActivity.loginViaDataManager() {
//    val loginNetworkCall = ApiService.create()
//            .login()
//            .map { it.token }
//    val dataManager: DataManager = DataManagerImpl(loginNetworkCall)

    dataManager.logIn()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { Toast.makeText(this@loginViaDataManager, "login success", Toast.LENGTH_LONG).show() },
                    { Toast.makeText(this@loginViaDataManager, "login ERROR: $it", Toast.LENGTH_LONG).show() }
            )
}

const val EVENT_DISPLAY_TIME = 7L
@TargetApi(Build.VERSION_CODES.M)
fun MainActivity.attachEventsToScreen() {

    val dataManager = object : DataManager {
        override fun logIn(): Completable = TODO("not implemented: .logIn")

        override fun logOut(): Completable = TODO("not implemented: .logOut")

        override fun getBalance(): Single<Pair<MoneyAmount, Currency>> = TODO("not implemented: .getBalance")

        override fun getTransactions(): Single<List<Transaction>> = TODO("not implemented: .getTransactions")

        override fun spend(description: String, amount: MoneyAmount, currency: Currency): Completable = TODO("not implemented: .spend")

        override fun observe(): Observable<DataManagerEvent> = Observable
                .range(1, 15)
                .flatMap { c ->
                    val delay = ((Math.random() + 0.4) * c * 2).toLong()
                    Observable.timer(delay, TimeUnit.SECONDS)
                        .map { DataManagerEvent.Requesting("test -- $c") }
                }

    }

    val recyclerView = RecyclerView(this).apply {
        adapter = EventsAdapter(dataManager, this@attachEventsToScreen)
        layoutManager = LinearLayoutManager(this@attachEventsToScreen,
                LinearLayoutManager.VERTICAL, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setBackgroundColor(getColor(android.R.color.holo_orange_light))
        }
    }
    val contentView = findViewById(android.R.id.content) as FrameLayout

    val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            400,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM)
    contentView.addView(recyclerView, layoutParams)
    recyclerView.bottom = 100

}
class EventsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textView: TextView = view.findViewById(R.id.text) as TextView
}
class EventsAdapter(val dataManager: DataManager, val context: Context) : RecyclerView.Adapter<EventsViewHolder>() {
    private val eventsQueue = ArrayDeque<String>()
    private var disposable: Disposable = Disposables.disposed()
    private var counter = 0

    override fun getItemCount(): Int {
        Log.i("AAA", "get item COUNT = ${eventsQueue.size}")
        return eventsQueue.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EventsViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_event, parent, false)
        return EventsViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        holder.textView.text = eventsQueue.elementAt(eventsQueue.size - position - 1)
//        holder.textView.text = eventsQueue.elementAt(position)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        disposable.dispose()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        disposable = dataManager.observe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showAndScheduleRemoval)
        super.onAttachedToRecyclerView(recyclerView)
    }

    private fun showAndScheduleRemoval(event: DataManagerEvent) {
        eventsQueue.add("[${counter++}] $event")
//        notifyItemInserted(eventsQueue.size - 1)
        Log.i("AAA", "notify item INSERTED")
        notifyItemInserted(0)
        // schedule a removal of the element
        Single.timer(EVENT_DISPLAY_TIME, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ ->
                    // if crashes because queue is empty then consider using '.poll()' instead
                    eventsQueue.remove()
                    Log.i("AAAA", "notify item REMOVED")
                    notifyItemRemoved(eventsQueue.size)
//                    notifyItemRemoved(0)
                }
    }

}
