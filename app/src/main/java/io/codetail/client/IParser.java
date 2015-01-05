package io.codetail.client;

public interface IParser<T extends Page> {

    public boolean canParse(String url);

    public T parse(T base, String source);

}
