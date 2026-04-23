import React, { useState } from 'react'
import Search from 'antd/es/input/Search'
import { InputRef } from 'antd'
import { SearchQuestionModal } from '../../../../domain/question'
import { NoteWithRelations } from '../../../../domain/note/types/serviceTypes.ts'
import { useNavigate } from 'react-router-dom'
import { NOTE_DETAIL } from '../../router/config.ts'

const SearchInput: React.FC = () => {
  const [keyword, setKeyword] = useState<string>('')
  const searchInputRef = React.createRef<InputRef>()

  const [isModalOpen, setIsModalOpen] = useState(false)

  const toggleIsModalOpen = () => {
    setIsModalOpen(!isModalOpen)
  }

  const navigate = useNavigate()

  return (
    <>
      <Search
        ref={searchInputRef}
        value={keyword}
        onSearch={(value) => setKeyword(value)}
        onChange={(e) => setKeyword(e.target.value)}
        onFocus={() => {
          toggleIsModalOpen()
          searchInputRef.current?.blur()
        }}
        placeholder="搜索问题标题或笔记内容"
        width={450}
      />
      <SearchQuestionModal
        isModalOpen={isModalOpen}
        toggleIsModalOpen={toggleIsModalOpen}
        onConfirm={(item: NoteWithRelations) => {
          navigate(`${NOTE_DETAIL}/${item.noteId}`)
        }}
      />
    </>
  )
}

export default SearchInput
