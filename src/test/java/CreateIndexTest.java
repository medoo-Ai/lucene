import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @auther SyntacticSugar
 * @data 2018/11/23 0023上午 11:37
 */


public class CreateIndexTest {
    /**
     * 给 文档添加fieldName /FieldText/store 是yes or no 控制是否保存
     */
    @Test
    public void createIndexTest() {
        Document document = new Document();
        document.add(new StringField("id", "1", Field.Store.YES));
        document.add(new TextField("title", "谷歌地图之父跳槽facebook厉害了我的哥碉堡了" +
                "蓝瘦香菇", Field.Store.YES));
        // 创建储存目录、分词
        try {
            FSDirectory indexDir = FSDirectory.open(new File("indexDir"));
//            StandardAnalyzer analyzer = new StandardAnalyzer();
            IKAnalyzer analyzer = new IKAnalyzer();
            /**
             * 创建文档配置对象,IndexWriterConfig.OpenMode.CREATE  清空创建索引
             * IndexWriterConfig.OpenMode.APPEND 在原来基础上追加
             */
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LATEST, analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            //索引
            IndexWriter indexWriter = new IndexWriter(indexDir, indexWriterConfig);
            //
            //写入索引，提交，关闭
            indexWriter.addDocument(document);
            indexWriter.commit();
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给 文档添加filedName /FiledText/store 是yes or no
     */
    @Test
    public void createIndexTest2() throws IOException {
        ArrayList<Document> documents = new ArrayList<>();
        Document document1 = new Document();
        document1.add(new StringField("id", "1", Field.Store.YES));
        document1.add(new TextField("title", "谷歌地图之父跳槽facebook厉害了我的哥碉堡了" +
                "蓝瘦香菇", Field.Store.YES));
        Document document2 = new Document();
        document2.add(new StringField("id", "2", Field.Store.YES));
        document2.add(new TextField("title", "谷歌地图之父拉斯加盟社交网站Facebook" +
                "蓝瘦香菇", Field.Store.YES));
        //添加到集合中
        documents.add(document1);
        documents.add(document2);
        // 创建储存目录、分词
        FSDirectory indexDir = FSDirectory.open(new File("indexDir"));
//            StandardAnalyzer analyzer = new StandardAnalyzer();
        IKAnalyzer analyzer = new IKAnalyzer();
        /**
         * 创建文档配置对象,IndexWriterConfig.OpenMode.CREATE  清空创建索引
         * IndexWriterConfig.OpenMode.APPEND 在原来基础上追加
         */
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LATEST, analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        //索引
        IndexWriter indexWriter = new IndexWriter(indexDir, indexWriterConfig);
        //
        //写入索引，提交，关闭s
        indexWriter.addDocuments(documents);
        indexWriter.commit();
        indexWriter.close();
    }

    /**
     * 创建查询索引数据
     */
    @Test
    public void testSearch() throws IOException, ParseException {
        FSDirectory indexDir = FSDirectory.open(new File("indexDir"));
        /**
         * 索引读取工具
         * 索引搜索工具
         */
        IndexReader open = DirectoryReader.open(indexDir);
        IndexSearcher indexSearcher = new IndexSearcher(open);
        //创建查询解析器[可以传参入一个数组，多条件进行查询]，创建要查询的对象
//        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        QueryParser parser = new MultiFieldQueryParser(new String[]{"id", "title"}, new IKAnalyzer());
        Query query = parser.parse("谷歌地图之父");
        //搜索
        //topDocs包含totalHits、scoreDocs
        TopDocs topDocs = indexSearcher.search(query, 5);//参数5，就是排名前五名的
        System.out.println("查找到数据的条数" + topDocs.totalHits);
        /**
         * 获取文档编号中文档的编号以及文档的得分
         *
         */
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docId = scoreDoc.doc;
            float score = scoreDoc.score;
            //根据文档编号查找 doc
            Document doc = indexSearcher.doc(docId);
            String id = doc.get("id");
            String title = doc.get("title");
            System.out.println(id + ":" + title);
        }
    }

    /**
     * search  抽取一个公共的查询
     */
    public void search(Query query) throws IOException {
        //建新目录，读取工具，搜索工具
        FSDirectory directory = FSDirectory.open(new File("indexDir"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        /**
         * 搜索符合条件的topDocs
         * 对文档进行解析  获取  totalHits   ,scoreDocs
         */
        TopDocs topDocs = searcher.search(query, 10);
        System.out.println("本次搜索到的条数：" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        //遍历
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docId = scoreDoc.doc;
            float score = scoreDoc.score;
            //查找到对应的文档
            Document doc = searcher.doc(docId);
            System.out.println(doc.get("id") + ":" + doc.get("title") + ":" + score);
        }
    }

    /**
     * testTermQuery  词条查询
     */
    @Test
    public void testTermQuery() throws IOException {
        // 创建词条查询的对象
        TermQuery title = new TermQuery(new Term("title", "谷歌"));
        //调用查询方法进行查询
        search(title);
    }

    /**
     * WildcardQuery 通配符查询
     */
    @Test
    public void WildcardQuery() throws IOException {
        TermQuery query = new TermQuery(new Term("title", "??"));
        search(query);
    }

    /**
     * testFuzzyQuery 通配符查询  Fuzzy:失真、模糊
     * fscevool 编辑距离0-2
     */
    @Test
    public void testFuzzyQuery() throws IOException {
        FuzzyQuery query = new FuzzyQuery(new Term("title", "谷歌啊"), 1);
        search(query);
    }

    /**
     * NumericRangeQuery  数字查询范围
     * 可以进行精确的查找
     * 参数：字段名称，最小值、最大值、是否包含最小值、是否包含最大值
     */
    @Test
    public void testNumericRangeQuery() throws IOException {
        //Query接口
        Query query = NumericRangeQuery.newLongRange("id", 2L, 2L, true, true);
        search(query);
    }

    /**
     * BooleanQuery（组合查询）
     * Occur.MUST_NOT  不是必须的
     * Occur.SHOULD    必须的
     */

    @Test
    public void testBooleanQuery() throws IOException {
        Query query1 = NumericRangeQuery.newLongRange("id", 2L, 2L, true, true);
        Query query2 = NumericRangeQuery.newLongRange("id", 1L, 4L, true, true);
        //创建boolean查询，然后添加query1/query2
        BooleanQuery query = new BooleanQuery();
        query.add(query1, BooleanClause.Occur.MUST_NOT);
        query.add(query2, BooleanClause.Occur.SHOULD);
        search(query);
    }

    /**
     * 修改索引
     * 先删除，后创建
     * 1、所以我们是修改   一把针对唯一的进行修改
     * 2、deleteDocuments 若id 是数字,直接删除即可
     */
    @Test
    public void testUpdate() throws IOException {
        //创建目录，创建IndexWriterConfig，创建索引写出对象
        FSDirectory directory = FSDirectory.open(new File("indexDir"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);
        //创建文档索引数据，而后进行修改
        Document document = new Document();
        document.add(new StringField("id", "1", Field.Store.YES));
        document.add(new TextField("title", "谷歌地图之父跳槽facebook 为了加入复仇者联盟 屌爆了啊", Field.Store.YES));
        /**
         * 修改
         * Term  词条
         * document   文档
         */
        indexWriter.updateDocument(new Term("id", "1"), document);
        indexWriter.commit();
        indexWriter.close();
    }

    /**
     * testDelete
     * 删除词条 ，通过id 来删除   indexWriter.deleteDocuments(new Term("id", "1"));
     * 1、删除所有  deleteAll
     * 2、条件删除   query来删除
     */
    @Test
    public void testDelete() throws IOException {
        //创建目录，创建IndexWriterConfig，创建索引写出对象
        FSDirectory directory = FSDirectory.open(new File("indexDir"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);
        //创建文档索引数据
//        indexWriter.deleteDocuments(new Term("id", "1"));
        Query query = NumericRangeQuery.newLongRange("id", 2L, 2L, true, true);
        indexWriter.deleteDocuments(query);
        indexWriter.commit();
        indexWriter.close();
    }


    /**
     * 代码高亮显示
     * testHighlighter
     */
    @Test
    public void testHighlighter() throws Exception {
        // 目录，directoryreader ，searcher
        FSDirectory directory = FSDirectory.open(new File("indexDir"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        //分词
        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        Query query = parser.parse("谷歌地图");
        searcher.search(query, 10);
        /**
         * 格式化器：
         * 准备高亮工具：
         */
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<em>", "</em>");
        QueryScorer queryScorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, queryScorer);
        //搜索文档数据
        TopDocs topDocs = searcher.search(query, 10);
        System.out.println("本次搜索到的条数：" + topDocs.totalHits);
        //bianli

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            // 获取文档编号，得分编号docId / score
            int docId = scoreDoc.doc;
            float score = scoreDoc.score;
            //查询文档的 title  、得分
            Document doc = searcher.doc(docId);
            String title = doc.get("title");
            //   高亮处理  Analyzer analyzer, String fieldName, String text
            String bestFragment = highlighter.getBestFragment(new IKAnalyzer(), "title", title);
            System.out.println("高亮显示:" + bestFragment);
            //打印得分到控制台
            System.out.println(score);
        }
    }

    /**
     * 排序
     * 创建排序对象 ，搜索
     */
    @Test
    public void testSortQuery() throws Exception {
        //  目录对象，目录读取，搜索工具
        FSDirectory directory = FSDirectory.open(new File("indexDir"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        //分词，创建查询对象 query
        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        Query query = parser.parse("谷歌地图");

        /**
         * 排序对象、查询
         * 排序字段参数，默认降序  String field, SortField.Type type, boolean reverse)
         */
        Sort sort = new Sort(new SortField("", SortField.Type.LONG, true));
        TopDocs topDocs = searcher.search(query, 1, sort);
        //
        //展示  topDocs文档中的数据
        System.out.println("共查询到数据条数：" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        //遍历
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docId = scoreDoc.doc;
            Document doc = searcher.doc(docId);
            System.out.println("文档编号：" + doc.get("id") + ":" + doc.get("title") + "得分：" + scoreDoc.score);
        }
    }

    /**
     * 分页 testPageQuery
     * lucene
     */

    @Test
    public void testPageQuery() {

    }

}
