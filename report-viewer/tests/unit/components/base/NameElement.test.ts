import { flushPromises, mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import NameElement from '@/components/NameElement.vue'
import { store } from '@/stores/store'

const store = {
  state: {
    isAnon: false
  },
  getDisplayName(id: string) {
    return id
  },
  isAnonymous() {
    return this.state.isAnon
  },
  addAnonymous() {
    this.state.isAnon = true
  },
  removeAnonymous() {
    this.state.isAnon = false
  }
}

describe('Test Name Display Element', () => {
  beforeEach(() => {
    vi.mock('@/stores/store', () => ({
      store: vi.fn(() => {
        return store
      })
    }))
  })

  it('Test correct display', () => {
    const wrapper = mount(NameElement, {
      props: {
        id: 'id'
      }
    })

    expect(wrapper.text()).toContain('id')
  })

  it('Test Anonymization', async () => {
    const deleteFunction = vi.spyOn(store, 'removeAnonymous')
    const addFunction = vi.spyOn(store, 'addAnonymous')
    const wrapper = mount(NameElement, {
      props: {
        id: 'id'
      }
    })

    wrapper.find('div.invisible').trigger('click')
    await flushPromises()
    expect(addFunction).toHaveBeenCalled()
    wrapper.find('div.invisible').trigger('click')
    await flushPromises()
    expect(deleteFunction).toHaveBeenCalled()
  })
})
