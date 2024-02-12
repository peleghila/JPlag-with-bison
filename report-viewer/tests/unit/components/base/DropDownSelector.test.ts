import { flushPromises, mount } from '@vue/test-utils'
import { describe, it, expect, beforeAll, vi } from 'vitest'
import DropDownSelector from '@/components/DropDownSelector.vue'
import { store } from '@/stores/store'

const store = {
  getDisplayName(s: string) {
    return s
  }
}

describe('Basic Dropdown Tests', () => {
  beforeAll(() => {
    vi.mock('@/stores/store', () => ({
      store: vi.fn(() => {
        return store
      })
    }))
  })

  it('emit on change', async () => {
    const options = ['Option1', 'Option2', 'Option3']

    const wrapper = mount(DropDownSelector, {
      props: {
        options: options
      }
    })

    async function expectOption(option: string, requestIndex: number) {
      wrapper.find('select').setValue(option)
      await flushPromises()
      const emit = wrapper.emitted('selectionChanged')
      expect(emit).toBeDefined()
      expect(emit?.length).toBeGreaterThan(requestIndex)
      expect(emit?.[requestIndex].length).toBeGreaterThanOrEqual(1)
      console.log(emit)
      expect(emit?.[requestIndex][0]).toEqual(option)
      expect(wrapper.text()).toContain(option)
    }

    expect(wrapper.text()).toContain(options[0])

    await expectOption(options[2], 0)
    await expectOption(options[1], 1)
  })
})
