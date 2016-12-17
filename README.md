# drag_and_drop_
Это тестовая работа.

Следующие строки написаны для тех, кто знает ТЗ =)

Пункт 3: при изменении порядка записей данные сохраняются так быстро, что не требуется это делать в отдельном потоке. 
Скорость работы не зависит от количества объектов.
Но если очень хочется, то есть метод updateNotesAsync(Notes... notes)

Пункт 4: может я чего-то, конечно, не понял, но двусвязный список совсем не обязателен для хранения объектов в БД. Хватило бы структуры односвязного списка)

Пункт 6: для UI drag&drop использовал рекомендуемую библиотеку

Ну и еще, было сказано, что плюсом будет использовать ContentProvider для CRUD.
Но вот что говорит google:
[https://developer.android.com/guide/topics/providers/content-provider-creating.html?hl=ru#BeforeYouStart](https://developer.android.com/guide/topics/providers/content-provider-creating.html?hl=ru#BeforeYouStart)

Поэтому не совсем понятно зачем для CRUDа, в нашем случае, использовать ContentProvider

![1](https://cloud.githubusercontent.com/assets/5888292/21286497/b39b8f86-c466-11e6-8651-72fb2db46795.png)
