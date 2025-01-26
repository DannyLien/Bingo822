package com.hank.bingo822

class Member(
    var uid: String,
    var displayName: String,
    var nickname: String?,
    var avatId: Int
) {
    constructor() : this("", "", null, 0)
}
