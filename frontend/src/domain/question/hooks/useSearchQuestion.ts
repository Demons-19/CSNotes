import { useEffect, useState } from 'react'
import debounce from 'lodash.debounce'
import { noteService } from '../../note/service/noteService.ts'
import { NoteWithRelations } from '../../note/types/serviceTypes.ts'

/**
 * 根据关键字搜索笔记（题目标题 + 笔记内容全文检索），添加 debounce 处理
 *
 * @param keyword 关键字
 */
export function useSearchQuestion(keyword: string) {
  /**
   * 笔记列表
   */
  const [noteList, setNoteList] = useState<NoteWithRelations[]>([])

  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (keyword === undefined || keyword.trim() === '') {
      setNoteList([])
      return
    }

    const searchNote = debounce(async () => {
      setLoading(true)
      const { data } = await noteService.searchFullText({
        keyword,
        page: 1,
        pageSize: 20,
      })
      setNoteList(data)
      setLoading(false)
    }, 300)

    searchNote()

    return () => {
      searchNote.cancel()
    }
  }, [keyword])

  return {
    loading,
    noteList,
  }
}
