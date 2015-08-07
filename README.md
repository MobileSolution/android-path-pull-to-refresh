# android-pull-to-refresh
Android pull to refresh library and iplementations

Usage

```XML
<com.eftimoff.pulltorefresh.PathPullToRefreshLayout
        android:id="@+id/pullToRefresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:pptrBackgroundColor="@android:color/black"
        app:pptrPathWidth="5"
        app:pptrSvgResourceId="@raw/lol">

        // Views
</com.eftimoff.pulltorefresh.PathPullToRefreshLayout>
```

##### Download

	repositories {
	    // ...
	    maven { url "https://jitpack.io" }
	}
	
	dependencies {
	        compile 'com.github.MobileSolution:android-path-pull-to-refresh:v0.1.1'
	}
