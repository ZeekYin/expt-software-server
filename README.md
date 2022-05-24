# expt-software-server

# request
## 新規登録
文字列`#registration#{"username":"passwd"}`を送る
できたら`{"uid":"uid:int","deposit":"deposit:int"}`が返される
すでに存在する場合は`#failed#`が返される。
example
#registration#{"junpei":"gashuin"}
## ログイン
文字列`#login#{"username":"passwd"}`を送る
できたら``{"uid":"uid:int"}`が返される
userが存在しない場合は`#notexist#`が返される。
passwordが正しくない場合は`#wrongpasswd#`が返される。

## コメントを送る
文字列`#comment#{"uid","username","roomid","comment"}`を送る
できたら`#success#`が返される

## 投げ銭
文字列`#tip#{"uid","roomid","amount"}`を送る
できたら残高`{"balance:int"}`が返される
残高が不足の場合は`#notenough#`が返される

## 配信者一覧を見る
文字列`#getroooms#`を送る
`{“roomid1”:”room name”, “roomid2”:”room name”}`が返される

## 配信部屋に入る
文字列`#listenroom#{"roomID:int"}`を送る
できたら`#success#`が返される
存在しない場合は`#nothisroom#`が返される

## 配信を見るのをやめる
文字列`#quitroom#{"roomID:int"}`を送る
できたら`#bye#`が返される

## 配信開始
文字列`#startstreamming#{"uid:string":"roomname:string":"port:int"}`を送る
できたら`{"roomID":"roomID:int"}`が返される
## 配信終了
文字列`#stop#{"roomID:int"}`を送る
できたら`#bye#`が返される

# response
## コメント
`#comment#{"username":"username:string","comment":"comment:string"}`が送られる

## 投げ銭
`#tip#{"username":"username:string","amount":"amount:int"}`が送られる

## 配信中止
`#LiveIsStopped#`

## 音声配信要求


## 音声配信要求
