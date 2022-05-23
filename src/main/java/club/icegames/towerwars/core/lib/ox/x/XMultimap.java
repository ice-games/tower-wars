package club.icegames.towerwars.core.lib.ox.x;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import ox.Log;

public class XMultimap<K, V> extends ForwardingMultimap<K, V> {

  private Multimap<K, V> delegate;

  public XMultimap(Multimap<K, V> delgate) {
    this.delegate = delgate;
  }

  /**
   * Note: unlike a normal multimap, changes to this returned list will NOT affect the underlying multimap.
   * 
   * If the key is missing, returns an empty list (as opposed to null).
   * 
   * {@code null} is a valid value of {@code key}. It is treated the same as any other key.
   */
  @Override
  public XList<V> get(K key) {
    Collection<V> c = super.get(key);
    return XList.create(c);
  }

  public XList<V> get(Collection<? extends K> keys) {
    XList<V> ret = XList.create();
    for (K key : keys) {
      ret.addAll(super.get(key));
    }
    return ret;
  }

  @Override
  public XList<V> values() {
    return XList.create(super.values());
  }

  @Override
  public XSet<K> keySet() {
    Set<K> set = super.keySet();
    return XSet.create(set);
  }

  public <V2> XMap<K, V2> toMap(Function<Collection<V>, V2> valueReducer) {
    XMap<K, V2> ret = XMap.create();
    for (K key : delegate.keySet()) {
      ret.put(key, valueReducer.apply(delegate.get(key)));
    }
    return ret;
  }

  public <T> XList<T> toList(BiFunction<K, XList<V>, T> mappingFunction) {
    XList<T> ret = XList.create();
    for (K key : super.keySet()) {
      ret.add(mappingFunction.apply(key, get(key)));
    }
    return ret;
  }

  public <K2> XMultimap<K2, V> transformKeys(Function<K, K2> keyFunction) {
    return transform(keyFunction, Function.identity());
  }

  public <V2> XMultimap<K, V2> transformValues(Function<V, V2> valueFunction) {
    return transform(Function.identity(), valueFunction);
  }

  public <K2, V2> XMultimap<K2, V2> transform(Function<K, K2> keyFunction, Function<V, V2> valueFunction) {
    XMultimap<K2, V2> ret = create();
    forEach((k, v) -> {
      ret.put(keyFunction.apply(k), valueFunction.apply(v));
    });
    return ret;
  }

  public XMultimap<K, V> filter(BiPredicate<K, V> predicate) {
    XMultimap<K, V> ret = create();
    forEach((k, v) -> {
      if (predicate.test(k, v)) {
        ret.put(k, v);
      }
    });
    return ret;
  }

  public XMap<K, XList<V>> asXMap() {
    XMap<K, XList<V>> ret = XMap.create();
    for (K key : this.keySet()) {
      ret.put(key, XList.create(get(key)));
    }
    return ret;
  }

  @Override
  protected Multimap<K, V> delegate() {
    return delegate;
  }

  public XMultimap<K, V> log() {
    this.forEach((k, v) -> {
      Log.debug(k + " = " + v);
    });
    return this;
  }

  public static <K, V> XMultimap<K, V> create() {
    return new XMultimap<>(LinkedListMultimap.create());
  }

}
