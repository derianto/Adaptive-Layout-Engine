package org.dk.aledemo.article;


import java.util.List;
import java.util.Map;


/**
 * A data accessor for articles.
 *
 *
 */
public interface ArticleDataAccessor
{
  List<Long> getArticleIds();

  ArticleInfo getArticle(long articleId);

  Map<Long, ArticleInfo> getArticles();
}
