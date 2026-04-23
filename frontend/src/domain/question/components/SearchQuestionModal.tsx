import React, { useEffect, useState } from 'react'
import { Empty, InputRef, Modal, Spin } from 'antd'
import { useSearchQuestion } from '../hooks/useSearchQuestion.ts'
import Search from 'antd/es/input/Search'
import SearchModalFooter from './SearchModalFooter.tsx'
import { NoteWithRelations } from '../../note/types/serviceTypes.ts'

interface SearchQuestionModalProps {
  isModalOpen: boolean
  toggleIsModalOpen: () => void
  onConfirm: (note: NoteWithRelations) => void
}

const scrollToSelected = (index: number) => {
  const element = document.querySelector(`.result-item-${index}`)
  if (element) {
    element.scrollIntoView({
      behavior: 'smooth',
      block: 'center',
    })
  }
}

const SearchQuestionModal: React.FC<SearchQuestionModalProps> = ({
  isModalOpen,
  toggleIsModalOpen,
  onConfirm,
}) => {
  const [keyword, setKeyword] = useState('')
  const inputRef = React.createRef<InputRef>()
  const [selectedIndex, setSelectedIndex] = useState(0)

  const { noteList, loading } = useSearchQuestion(keyword)

  useEffect(() => {
    if (!isModalOpen) {
      setKeyword('')
      setSelectedIndex(0)
    }
  }, [isModalOpen])

  useEffect(() => {
    setSelectedIndex(0)
  }, [keyword])

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (!isModalOpen) return

      if (event.key === 'Enter') {
        if (noteList[selectedIndex]) {
          onConfirm(noteList[selectedIndex])
          toggleIsModalOpen()
        }
      }

      if (event.key === 'ArrowUp') {
        setSelectedIndex((prevState) => {
          const newIndex = Math.max(0, prevState - 1)
          scrollToSelected(newIndex)
          return newIndex
        })
      }

      if (event.key === 'ArrowDown') {
        setSelectedIndex((prevState) => {
          const newIndex = Math.min(noteList.length - 1, prevState + 1)
          scrollToSelected(newIndex)
          return newIndex
        })
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isModalOpen, noteList, selectedIndex, onConfirm, toggleIsModalOpen])

  return (
    <Modal
      open={isModalOpen}
      footer={<SearchModalFooter />}
      onCancel={toggleIsModalOpen}
      title={'搜索笔记'}
      afterOpenChange={() => {
        if (inputRef.current) {
          inputRef.current.focus()
        }
      }}
      width={'40%'}
    >
      <div className="mt-4">
        <Search
          ref={inputRef}
          value={keyword}
          onSearch={(value) => setKeyword(value)}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="搜索问题标题或笔记内容"
        />
      </div>
      <div className="h-96 max-h-96 overflow-x-auto">
        <div className="test-xs my-2 font-medium text-gray-700">搜索结果</div>
        {loading ? (
          <div className="flex h-40 items-center justify-center">
            <Spin />
          </div>
        ) : (
          <>
            {noteList.map((item, index) => (
              <div
                key={item.noteId}
                className={`result-item-${index} cursor-pointer select-none border-b border-dashed p-3 transition-colors duration-200 ${index === selectedIndex ? 'bg-gray-200' : 'hover:bg-gray-50'}`}
                onClick={() => {
                  setSelectedIndex(index)
                }}
                onDoubleClick={() => {
                  onConfirm(item)
                  toggleIsModalOpen()
                }}
              >
                <div className="mb-1 text-sm font-semibold text-neutral-800">
                  {item.question.title}
                </div>
                <div className="mb-1 text-xs text-neutral-500">
                  作者：{item.author.username}
                </div>
                <div className="line-clamp-2 text-sm text-neutral-600">
                  {item.needCollapsed ? item.displayContent : item.content}
                </div>
              </div>
            ))}
            {noteList.length === 0 && <Empty description={'暂无搜索结果'} />}
          </>
        )}
      </div>
    </Modal>
  )
}

export default SearchQuestionModal
