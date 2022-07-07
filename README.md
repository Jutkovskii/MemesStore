# MemesStore
На случай важных переговоров. Приложение для хранения и пересылки мемов
## Описание и назначение
Приложение представляет собой галерею с собственной базой данных для хранения мемов в фото и видеоформате, а так же в виде ссылок на youtubе, с возможностью дальнейшей пересылки в сторонние приложения
## Особенности и требования
Требуется доступ к файловой системе телефона.
Добавление медиа реализовано как в виде приёма Intent'ов от сторонних приложений, так и при помощи вызова системной галереи
К каждому мему может быть добавлена подпись. Подпись используется для быстрого мемов в базе
База данных может быть испортирована/экспортирована вместе со всеми медиа в качестве Zip- архива