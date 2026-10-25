package com.pengyouxia.app.data.demo

import com.pengyouxia.app.data.FriendDraft
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.FriendResult
import com.pengyouxia.core.model.MemoCategory
import javax.inject.Inject

/** 演示数据填充：全部合成名字，只经仓库写入（供测试工具与截图用）。 */
class DemoDataSeeder @Inject constructor(private val repository: FriendRepository) {

    /** 追加演示数据，返回新增朋友数。 */
    suspend fun seed(): Int {
        var count = 0
        demo.forEach { d ->
            val id = (repository.addFriend(
                FriendDraft(d.name, d.nickname, d.metNote)
            ) as FriendResult.Success).value
            count++
            d.memos.forEach { (category, content, done) ->
                val memoId = (repository.addMemo(id, category, content)
                        as FriendResult.Success).value
                if (done) repository.setMemoDone(memoId, true)
            }
        }
        return count
    }

    private data class DemoFriend(
        val name: String,
        val nickname: String?,
        val metNote: String?,
        val memos: List<Triple<MemoCategory, String, Boolean>>,
    )

    private companion object {
        val demo = listOf(
            DemoFriend("王小明", "小明", "大学室友", listOf(
                Triple(MemoCategory.FAMILY, "女儿叫朵朵，今年上小学", false),
                Triple(MemoCategory.LIKES, "喜欢露营和手冲咖啡", false),
                Triple(MemoCategory.WANTS, "一副降噪耳机", true),
                Triple(MemoCategory.WANTS, "一盏露营灯", false),
                Triple(MemoCategory.RECENT, "刚换了新工作，在做产品经理", false),
            )),
            DemoFriend("李雷", null, "高中同桌", listOf(
                Triple(MemoCategory.LIKES, "不吃香菜", false),
                Triple(MemoCategory.RECENT, "下个月搬家到城东", false),
            )),
            DemoFriend("韩梅梅", "梅梅", "高中同学", listOf(
                Triple(MemoCategory.FAMILY, "先生是老师", false),
                Triple(MemoCategory.WANTS, "一本《小王子》精装版", false),
            )),
            DemoFriend("陈晓", "晓晓", "前同事", listOf(
                Triple(MemoCategory.LIKES, "爱喝无糖乌龙茶", false),
                Triple(MemoCategory.OTHER, "借了我的帐篷还没还", false),
            )),
            DemoFriend("老王", "王哥", "楼下邻居", listOf(
                Triple(MemoCategory.FAMILY, "儿子在读高三", false),
                Triple(MemoCategory.RECENT, "最近在学钓鱼", false),
            )),
            DemoFriend("Alex", null, "公司同事", listOf(
                Triple(MemoCategory.LIKES, "Loves board games and spicy food", false),
                Triple(MemoCategory.WANTS, "一个机械键盘", false),
            )),
            DemoFriend("😀小明", null, "网友", listOf(
                Triple(MemoCategory.OTHER, "每年元旦一起跨年", false),
            )),
            DemoFriend("3班老同学", null, "初中同学群", listOf(
                Triple(MemoCategory.RECENT, "打算明年聚会", false),
            )),
            DemoFriend("小王", "健身房小王", "健身房认识", listOf(
                Triple(MemoCategory.LIKES, "每周练三次腿", false),
            )),
            DemoFriend("小王", "隔壁小王", "老家邻居", listOf(
                Triple(MemoCategory.FAMILY, "奶奶身体不太好", false),
                Triple(MemoCategory.RECENT, "在老家开了一家面馆", false),
            )),
            DemoFriend("周可可", "可可", "朋友的朋友", listOf(
                Triple(MemoCategory.WANTS, "一台拍立得", false),
                Triple(MemoCategory.OTHER, LONG_MEMO, false),
            )),
            // 无备忘、无怎么认识的：验证列表不显示第二行
            DemoFriend("林悦", null, null, emptyList()),
        )

        const val LONG_MEMO =
            "计划明年春天一起自驾去西南：她想走沿海线慢慢开，中途在小城住两三晚找本地小吃；" +
                "不喜欢早起赶路，所以每天行程别排太满，下午四点前到住处；海鲜过敏，点菜要避开；" +
                "拍照喜欢胶片风，打算带旧相机；回程想顺路看一次日出，但要留一整天休息缓冲。"
    }
}
