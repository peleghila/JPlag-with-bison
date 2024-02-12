import ButtonComponent from '@/components/ButtonComponent.vue'
import { flushPromises, mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'

describe('Basic Button Tests', () => {
  it('Click emited', async () => {
    const wrapper = mount(ButtonComponent)
    wrapper.trigger('click')
    await flushPromises()
    expect(wrapper.emitted()).toHaveProperty('click')
  })
})
