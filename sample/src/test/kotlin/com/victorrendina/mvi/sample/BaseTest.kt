package com.victorrendina.mvi.sample

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.BeforeClass
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

abstract class BaseTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun classSetup() {
            RxJavaPlugins.reset()
            val immediate = object : Scheduler() {
                // this prevents StackOverflowErrors when scheduling with a delay
                override fun scheduleDirect(@NonNull run: Runnable, delay: Long, @NonNull unit: TimeUnit): Disposable =
                    super.scheduleDirect(run, 0, unit)

                override fun createWorker() = ExecutorScheduler.ExecutorWorker(Executor { it.run() })
            }
            RxJavaPlugins.setInitNewThreadSchedulerHandler { immediate }
            RxJavaPlugins.setInitComputationSchedulerHandler { immediate }
            RxJavaPlugins.setInitIoSchedulerHandler { immediate }
            RxJavaPlugins.setInitSingleSchedulerHandler { immediate }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { immediate }
            // This is necessary to prevent rxjava from swallowing errors
            // https://github.com/ReactiveX/RxJava/issues/5234
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                println("Uncaught exception handler called!")
                if (e is CompositeException && e.exceptions.size == 1) throw e.exceptions[0]
                throw e
            }
        }
    }
}