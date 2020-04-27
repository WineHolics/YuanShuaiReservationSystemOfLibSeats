package ys.service;

import java.util.List;

public interface BookService<T> {
	// 查询所有
	public abstract List<T> findBook(T t) throws Exception;

	// 数量
	public abstract int countBook(T t) throws Exception;

	// 新增
	public abstract void addBook(T t) throws Exception;

	// 修改
	public abstract void updateBook(T t) throws Exception;

	// 删除
	public abstract void deleteBook(Integer id) throws Exception;

	public abstract T findOneBook(int id) throws Exception;
}
