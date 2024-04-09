import CodePanel from '@/components/fileDisplaying/CodePanel.vue'
import ToolTipComponent from '@/components/ToolTipComponent.vue'
import { ParserLanguage } from '@/model/Language'
import { describe, it, expect } from 'vitest'
import { VueWrapper, flushPromises, mount } from '@vue/test-utils'

const fileContent = ['function test() {', '  console.log("test")', '}']

const file = {
  fileName: 'file.ts',
  data: fileContent.join('\n'),
  submissionId: 'submissionId',
  tokenCount: 11,
  matchedTokenCount: 5
}

const match = {
  start: 2,
  end: 2,
  match: {
    colorIndex: 1
  }
}

const props = {
  file: file,
  matches: [match],
  highlightLanguage: ParserLanguage.TYPESCRIPT
}

describe('CodePanel', () => {
  it('Render code', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    const text = wrapper.text()
    for (const line of fileContent) {
      expect(text).toContain(line)
    }
  })

  it('Render file name', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    expect(wrapper.text()).toContain(file.fileName)
  })

  it('Render matches', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    const table = wrapper.get('table')
    const line = table.findAll('tr')[match.start - 1]
    const code = line.findAll('td')[1]
    expect(code.attributes().style).toMatch(
      /rgba\([0-9]{1,3}, [0-9]{1,3}, [0-9]{1,3}, [0-9]\.[0-9]+\)/
    )
  })

  it('Highlight code', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    // Check that hljs added a span for highlighting
    expect(wrapper.html()).toMatch(/<span[^>]*>function/)
  })

  it('Show token count', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    expect(wrapper.text()).toContain('10 tokens')
    expect(wrapper.text()).toContain('5 are part')
    expect(wrapper.text()).toContain('50%')
  })

  it('Expand code', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    const clickable = wrapper.getComponent(ToolTipComponent)

    expect(getTableVisibility(wrapper)).toBe(false)
    await clickable.trigger('click')
    await flushPromises()
    expect(getTableVisibility(wrapper)).toBe(true)
    await clickable.trigger('click')
    await flushPromises()
    expect(getTableVisibility(wrapper)).toBe(false)
  })

  it('Emit line click', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    const text = wrapper.findAll('tr')[1]
    text.trigger('click')
    await flushPromises()

    const emit = wrapper.emitted()['lineSelected']
    expect(emit).toBeDefined()
    expect(emit.length).toBe(1)
  })

  it('Collapse table on function call', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    const clickable = wrapper.getComponent(ToolTipComponent)

    expect(getTableVisibility(wrapper)).toBe(false)
    await clickable.trigger('click')
    await flushPromises()
    expect(getTableVisibility(wrapper)).toBe(true)
    wrapper.vm.collapse()
    await flushPromises()
    expect(getTableVisibility(wrapper)).toBe(false)
  })

  // Disabled cause jsdom can at the moment not handle scrolling
  it.skip('Show table on scroll to', async () => {
    const wrapper = mount(CodePanel, {
      props: props
    })
    await flushPromises()

    wrapper.vm.scrollTo(2)
    expect(getTableVisibility(wrapper)).toBe(true)
  })
})

function getTableVisibility(wrapper: VueWrapper) {
  const table = wrapper.get('table')
  const parent = table.element.parentElement
  return !parent?.classList.contains('hidden')
}
