# drag_and_drop_
Это тестовая работа. 

Следующие строки написаны для тех, кто знает ТЗ =)

Пункт 3: при изменении порядка записей данные сохраняются так быстро, что не требуется это делать в отдельном потоке. 
Но если очень хочется, то есть метод updateNotesAsync(Notes... notes)

Пункт 6: для UI drag&drop использовал рекомендуемую в пункте библиотеку

Ну и еще, было сказано, что плюсом будет использовать ContentProvider для CRUD.
Но вот что говорит google:
[https://developer.android.com/guide/topics/providers/content-provider-creating.html?hl=ru#BeforeYouStart](https://developer.android.com/guide/topics/providers/content-provider-creating.html?hl=ru#BeforeYouStart)

Поэтому не совсем понятно зачем для CRUDа в нашем случае использовать ContentProvider
