package jp.reflexworks.servlet.util;

import com.danga.MemCached.SockIOPool;
import com.danga.MemCached.MemCachedClient;

public class MemcacheUtils {
	
	private MemCachedClient cache;
	
	private static final int DEFAULT_PORT = 11211;
	private long SLEEP_MILLISEC = 200;
	private int NUM_RETRIES = 3;

	public MemcacheUtils(String host) {
		this(host, DEFAULT_PORT);
	}
	
	public MemcacheUtils(String host, int port) {
		SockIOPool pool = SockIOPool.getInstance();
		pool.setServers(new String[]{host + String.valueOf(port)});
		pool.initialize();

		cache = new MemCachedClient();
	}
	
	/**
	 * Memcacheへ値を登録します
	 * @param key キー
	 * @param value 値
	 */
	public void put(String key, Object value) {
		cache.set(key, value);
	}

	/**
	 * Memcacheから値を取得します
	 * @param key キー
	 * @return 値
	 */
	public Object get(String key) {
		return cache.get(key);
	}

	/**
	 * Memcacheの値を削除します
	 * @param key キー
	 */
	public void delete(String key) {
		cache.delete(key);
	}

	/**
	 * Memcacheに指定されたキーが登録されているかチェックします
	 * @param key キー
	 * @return true:登録あり、false:登録なし
	 */
	public boolean contains(String key) {
		return cache.keyExists(key);
	}
	
	/**
	 * Memcacheに指定された数値を加えます
	 * @param key キー
	 * @param value 値
	 * @return 指定された数値を加えた値
	 */
	public long increment(String key, long value) {
		if (value < 0) {
			return cache.decr(key, value * -1);
		}
		return cache.incr(key, value);
	}

	/**
	 * ロック取得
	 * ロックを解除したい場合、releaseLockを実行してください。
	 * @param key キー
	 * @return true:ロック成功、false:ロック失敗
	 */
	public boolean acquireLock(String key) {
		if (!contains(key)) {
			put(key, 1l);	// 初期処理
			return true;
		}
		
		for (int r = 0; r <= NUM_RETRIES; r++) {
			long ret = increment(key, 1l);
			if (ret == 1) {
				return true;	// ロック成功
			}
			
			increment(key, -1l);	// ロック失敗、元に戻す。
			if (SLEEP_MILLISEC > 0) {
				try {
					Thread.sleep(SLEEP_MILLISEC);
				} catch (InterruptedException e2) {}
			}
		}
		
		return false;
	}
	
	/**
	 * ロックされているかどうかの判定
	 * acquireLock処理でロックされている場合true、ロックされていない場合falseを返します。
	 * @param key キー
	 * @return true:ロック中、false:ロックされていない
	 */
	public boolean isLock(String key) {
		if (!contains(key)) {
			return false;
		}
		long ret = (Long)get(key);
		if (ret == 0) {
			return false;
		}
		return true;
	}

	/**
	 * ロック解除
	 * @param key キー
	 */
	public void releaseLock(String key) {
		increment(key, -1l);
	}
	
	/**
	 * ロック処理を行う場合の、リトライ時のスリープ時間を設定します。
	 * @param millisec リトライ時のスリープ時間（ミリ秒）
	 */
	public void setSleepMillsec(long millisec) {
		SLEEP_MILLISEC = millisec;
	}
	
	/**
	 * ロック処理を行う場合の、リトライ回数を設定します。
	 * @param count リトライ回数
	 */
	public void setRetryCount(int count) {
		if (count < 0) {
			NUM_RETRIES = 0;
		} else {
			NUM_RETRIES = count;
		}
	}

}
